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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Accesses Datastore to support managing ScheduledInterview entities. */
public class FakeScheduledInterviewDao implements ScheduledInterviewDao {
  // @param datastore The DatastoreService we're using to interact with Datastore.
  private LinkedHashMap<String, ScheduledInterview> datastore;

  /** Initializes the fields for ScheduledInterviewDatastoreDAO. */
  public FakeScheduledInterviewDao() {
    datastore = new LinkedHashMap<String, ScheduledInterview>();
  }

  /**
   * Retrieves a scheduledInterviewEntity from Datastore and returns it as a ScheduledInterview
   * object.
   */
  @Override
  public Optional<ScheduledInterview> get(long id) {
    if (datastore.containsKey(Long.toString(id))) {
      return Optional.of(datastore.get(Long.toString(id)));
    }
    return Optional.empty();
  }

  /**
   * Retrieves all scheduledInterview entities from Datastore that involve a particular user and
   * returns them as a list of ScheduledInterview objects in the order in which they occur.
   */
  @Override
  public List<ScheduledInterview> getForPerson(String email) {
    Set set = datastore.entrySet();
    Iterator iterator = set.iterator();
    List<ScheduledInterview> relevantInterviews = new ArrayList<>();
    /*
    while (iterator.hasNext()) {
      Map.Entry scheduledInterview = (Map.Entry) iterator.next();
      if (email.equals(
          scheduledInterview.getValue().interviewerEmail()
              || email.equals(scheduledInterview.getValue().intervieweeEmail()))) {
        relevantInterviews.add(scheduledInterview.getValue());
      }
    }
    */
    return relevantInterviews;
  }

  /** Creates a ScheduledInterview Entity and stores it in Datastore. */
  @Override
  public void create(ScheduledInterview scheduledInterview) {
    datastore.put(Long.toString(scheduledInterview.id()), scheduledInterview);
  }

  /** Updates an entity in datastore. */
  @Override
  public void update(ScheduledInterview scheduledInterview) {
    delete(scheduledInterview.id());
    create(scheduledInterview);
  }

  /** Deletes an entity in datastore. */
  @Override
  public void delete(long id) {
    datastore.remove(Long.toString(id));
  }

  /** Creates a ScheduledInterview object from a datastore entity. */
  public ScheduledInterview entityToScheduledInterview(Entity scheduledInterviewEntity) {
    return ScheduledInterview.create(
        scheduledInterviewEntity.getKey().getId(),
        new TimeRange(
            Instant.ofEpochMilli((long) scheduledInterviewEntity.getProperty("startTime")),
            Instant.ofEpochMilli((long) scheduledInterviewEntity.getProperty("endTime"))),
        (String) scheduledInterviewEntity.getProperty("interviewer"),
        (String) scheduledInterviewEntity.getProperty("interviewee"));
  }

  /** Creates a scheduledInterview Entity from a scheduledInterview object. */
  public Entity scheduledInterviewToEntity(ScheduledInterview scheduledInterview) {
    Entity scheduledInterviewEntity = new Entity("ScheduledInterview");
    scheduledInterviewEntity.setProperty(
        "startTime", scheduledInterview.when().start().toEpochMilli());
    scheduledInterviewEntity.setProperty("endTime", scheduledInterview.when().end().toEpochMilli());
    scheduledInterviewEntity.setProperty("interviewer", scheduledInterview.interviewerEmail());
    scheduledInterviewEntity.setProperty("interviewee", scheduledInterview.intervieweeEmail());
    return scheduledInterviewEntity;
  }
}
