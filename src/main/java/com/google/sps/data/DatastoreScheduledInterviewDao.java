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

import java.nio.file.DirectoryStream.Filter;
import java.time.LocalDate;
import java.util.Optional;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.sps.ScheduledInterview;

/** Accesses Datastore to support managing ScheduledInterview entities. */
public class DatastoreScheduledInterviewDao implements ScheduledInterviewDao {
  // @param datastore the DatastoreService we're using to interact with Datastore.
  private DatastoreService datastore;

  /** Initializes the fields for ScheduledInterviewDatastoreDAO. */
  public DatastoreScheduledInterviewDao() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Retrieve a scheduledInterviewEntity from Datastore
   * and return it as a scheduledInterview object.
   */
  public Optional get(String email) {
    try {
      Key key = KeyFactory.createKey("attendee", email);
      Optional<Entity> scheduledInterviewEntity = datastore.get(key); 
    } catch (com.google.appengine.api.datastore.EntityNotFoundException e) {
      return Optional.empty(); 
    }
    Filter interviewerFilter = new FilterPredicate("interviewer", FilterOperator.EQUAL, email);
    return scheduledInterviewEntity;
  }

  /**
   * Saves an entity to datastore.
   */
  public void put(Entity scheduledInterviewEntity) {
    datastore.put(scheduledInterviewEntity);
  }

  /**
   * Updates an entity in datastore.
   */
  public void update(Entity scheduledInterviewEntity) {
    datastore.put(scheduledInterviewEntity);
  }

  /**
   * Creates a ScheduledInterview Entity.
   */
  private static Entity create(ScheduledInterview scheduledInterview) {
    Entity scheduledInterviewEntity = new Entity("ScheduledInterview");
    scheduledInterviewEntity.setProperty("when", scheduledInterview.getWhen());
    scheduledInterviewEntity.setProperty("date", scheduledInterview.getDate());
    scheduledInterviewEntity.setProperty("interviewer", scheduledInterview.getInterviewerEmail());
    scheduledInterviewEntity.setProperty("interviewee", scheduledInterview.getIntervieweeEmail());
    return scheduledInterviewEntity;
  }

  /** Creates a ScheduledInterview object from a datastore entity */
  private static ScheduledInterview entityToScheduledInterview(Entity scheduledInterviewEntity) {
    return new ScheduledInterview(
        (TimeRange) scheduledInterviewEntity.getProperty("when"),
        (LocalDate) scheduledInterviewEntity.getProperty("date"),
        (String) scheduledInterviewEntity.getProperty("interviewer"),
        (String) scheduledInterviewEntity.getProperty("interviewee"));
  }
}
