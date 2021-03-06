// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Accesses Datastore to support managing Availability entities. */
public class DatastoreAvailabilityDao implements AvailabilityDao {
  // @param datastore the DatastoreService we're using to interact with Datastore.
  private DatastoreService datastore;

  /** Initializes the fields for DatastoreAvailabilityDao. */
  public DatastoreAvailabilityDao() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Retrieve the availability from Datastore from its id and wrap it in an Optional. If the id
   * isn't in Datastore, the Optional is empty.
   */
  @Override
  public Optional<Availability> get(long id) {
    Key key = KeyFactory.createKey("Availability", id);
    Entity availabilityEntity;
    try {
      availabilityEntity = datastore.get(key);
    } catch (com.google.appengine.api.datastore.EntityNotFoundException e) {
      return Optional.empty();
    }
    return Optional.of(entityToAvailability(availabilityEntity));
  }

  // Adds an Availability object into Datastore.
  @Override
  public void create(Availability avail) {
    datastore.put(availabilityToNewEntity(avail));
  }

  // Updates the specified id with the new availability.
  @Override
  public void update(Availability avail) {
    datastore.put(availabilityToUpdatedEntity(avail));
  }

  static Entity availabilityToNewEntity(Availability avail) {
    Entity availabilityEntity = new Entity("Availability");
    availabilityEntity.setProperty("userId", avail.userId());
    availabilityEntity.setProperty("startTime", avail.when().start().toEpochMilli());
    availabilityEntity.setProperty("endTime", avail.when().end().toEpochMilli());
    availabilityEntity.setProperty("scheduled", avail.scheduled());
    return availabilityEntity;
  }

  static Entity availabilityToUpdatedEntity(Availability avail) {
    Entity availabilityEntity = new Entity("Availability", avail.id());
    availabilityEntity.setProperty("userId", avail.userId());
    availabilityEntity.setProperty("startTime", avail.when().start().toEpochMilli());
    availabilityEntity.setProperty("endTime", avail.when().end().toEpochMilli());
    availabilityEntity.setProperty("scheduled", avail.scheduled());
    return availabilityEntity;
  }

  static Availability entityToAvailability(Entity availabilityEntity) {
    return Availability.create(
        (String) availabilityEntity.getProperty("userId"),
        TimeRange.fromStartEnd(
            Instant.ofEpochMilli((long) availabilityEntity.getProperty("startTime")),
            Instant.ofEpochMilli((long) availabilityEntity.getProperty("endTime"))),
        availabilityEntity.getKey().getId(),
        (boolean) availabilityEntity.getProperty("scheduled"));
  }

  // Deletes all Availability entities for a user ranging from minTime to maxTime.
  @Override
  public void deleteInRangeForUser(String userId, Instant minTime, Instant maxTime) {
    Filter userFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    List<Entity> entities = getEntitiesInRange(minTime, maxTime, Optional.of(userFilter));
    List<Key> keyList = new ArrayList<>();
    for (Entity entity : entities) {
      keyList.add(entity.getKey());
    }
    // This iterative deletion avoids XG transactions, which max out at 25 root entities.
    // We include the individual deletion transactions within a larger group transaction
    // to avoid replacing Availabilities before they are deleted.
    Transaction groupTxn = datastore.beginTransaction();
    for (Key key : keyList) {
      Transaction txn = datastore.beginTransaction();
      datastore.delete(txn, key);
      txn.commit();
    }
    groupTxn.commit();
  }

  // Returns a sorted (by ascending start times) list of all Availabilities ranging from
  // minTime to maxTime of a user.
  @Override
  public List<Availability> getInRangeForUser(String userId, Instant minTime, Instant maxTime) {
    Filter userFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    List<Entity> entities = getEntitiesInRange(minTime, maxTime, Optional.of(userFilter));
    List<Availability> availability = new ArrayList<Availability>();
    for (Entity entity : entities) {
      availability.add(entityToAvailability(entity));
    }
    return availability;
  }

  // Returns all Availabilities across all users ranging from minTime to maxTime in order
  // (by ascending start times).
  @Override
  public List<Availability> getInRangeForAll(Instant minTime, Instant maxTime) {
    List<Entity> entities = getEntitiesInRange(minTime, maxTime, Optional.empty());
    List<Availability> availability = new ArrayList<Availability>();
    for (Entity entity : entities) {
      availability.add(entityToAvailability(entity));
    }
    return availability;
  }

  private List<Entity> getEntitiesInRange(
      Instant minTime, Instant maxTime, Optional<Filter> filterOpt) {
    Filter startTimeFilter =
        new FilterPredicate(
            "startTime", FilterOperator.GREATER_THAN_OR_EQUAL, minTime.toEpochMilli());
    // Queries can only perform inequality filters on one parameter, and so instead
    // of using endTime for the endTimeFilter, startTime is used and the maxTime has 15
    // minutes subtracted from it to be equal to the latest possible startTime.
    Filter endTimeFilter =
        new FilterPredicate(
            "startTime",
            FilterOperator.LESS_THAN_OR_EQUAL,
            maxTime.minus(15, ChronoUnit.MINUTES).toEpochMilli());
    CompositeFilter startAndEndFilter = CompositeFilterOperator.and(startTimeFilter, endTimeFilter);
    if (filterOpt.isPresent()) {
      startAndEndFilter = CompositeFilterOperator.and(startAndEndFilter, filterOpt.get());
    }

    Query availQuery =
        new Query("Availability")
            .setFilter(startAndEndFilter)
            .addSort("startTime", SortDirection.ASCENDING);
    return datastore.prepare(availQuery).asList(FetchOptions.Builder.withDefaults());
  }

  // Returns the ids of all users that have availabilities within the specified time range.
  public Set<String> getUsersAvailableInRange(Instant minTime, Instant maxTime) {
    Filter startTimeFilter =
        new FilterPredicate(
            "startTime", FilterOperator.GREATER_THAN_OR_EQUAL, minTime.toEpochMilli());
    // Queries can only perform inequality filters on one parameter, and so instead
    // of using endTime for the endTimeFilter, startTime is used and the maxTime has 15
    // minutes subtracted from it to be equal to the latest possible startTime.
    Filter endTimeFilter =
        new FilterPredicate(
            "startTime",
            FilterOperator.LESS_THAN_OR_EQUAL,
            maxTime.minus(15, ChronoUnit.MINUTES).toEpochMilli());
    CompositeFilter startAndEndFilter = CompositeFilterOperator.and(startTimeFilter, endTimeFilter);
    Query availQuery = new Query("Availability").setFilter(startAndEndFilter);
    availQuery.addProjection(new PropertyProjection("userId", String.class));
    availQuery.addProjection(new PropertyProjection("startTime", Long.class));
    availQuery.setDistinct(true);
    List<Entity> results =
        datastore.prepare(availQuery).asList(FetchOptions.Builder.withDefaults());
    Set<String> userIds = new HashSet<String>();
    for (Entity result : results) {
      userIds.add((String) result.getProperty("userId"));
    }
    return userIds;
  }
}
