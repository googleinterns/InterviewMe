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
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Accesses Datastore to support managing Availability entities. */
public class DatastoreAvailabilityDao implements AvailabilityDao {
  // @param datastore the DatastoreService we're using to interact with Datastore.
  private DatastoreService datastore;

  /** Initializes the fields for DatastoreAvailabilityDao. */
  public DatastoreAvailabilityDao() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }
  
  /**
   * Retrieve the availability from Datastore from its id and wrap it in an Optional. If the id isn't in Datastore, the Optional is empty.
   */
  @Override
  public Optional<Availability> get(long id) {
    // TODO: handle this with the profile page servlet
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
    datastore.put(personToEntity(person));
  }
  
  // Updates the specified id with the new availability.
  @Override
  public void update(Availability avail) {
    datastore.put(personToEntity(person));
  }

  private static Entity availabilityToEntity(Availability avail) {
    Entity availabilityEntity = new Entity("Availability", avail.id());
    availabilityEntity.setProperty("email", avail.email());
    availabilityEntity.setProperty("startTime", getTimeInMillis(avail.date(), avail.when().start()));
    availabilityEntity.setProperty("endTime", getTimeInMillis(avail.date(), avail.when().end()));
    return availabilityEntity;
  }
  
  private static long getMillisFromDateAndInstant(LocalDate date, Instant instant) {
    return 0L;
  }
  
  private static LocalDate dateFromMillis(long millis) {
    return new LocalDate();
  }
  
  private static Instant instantFromMillis(long millis) {
    return new Instant();
  }

  private static Availability entityToAvailability(Entity availabilityEntity) {
    return Availability.create(
        (String) availabilityEntity.getProperty("email"),
        TimeRange.fromStartEnd(instantFromMillis())
        (String) availabilityEntity.getProperty("lastName"),
        (String) availabilityEntity.getProperty("company"),
        (String) availabilityEntity.getProperty("job"),
        (String) availabilityEntity.getProperty("linkedIn"));
  }
}
