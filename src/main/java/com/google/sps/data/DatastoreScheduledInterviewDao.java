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

package com.google.sps;

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
import com.google.sps.ScheduledInterview;
import com.google.sps.ScheduledInterviewDao;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Accesses Datastore to support managing ScheduledInterview entities. */
public class DatastoreScheduledInterviewDao implements ScheduledInterviewDao {
  // @param datastore the DatastoreService we're using to interact with Datastore.
  private DatastoreService datastore;

  /** Initializes the fields for ScheduledInterviewDatastoreDAO. */
  public DatastoreScheduledInterviewDao() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Saves an entity to datastore.
   */
  public void put(Entity scheduledInterviewEntity) {
    datastore.put(scheduledInterviewEntity);
  }

  /**
   * Creates a ScheduledInterview Entity.
   */
  private static Entity create(ScheduledInterview scheduledInterview) {
    Entity scheduledInterviewEntity = new Entity("ScheduledInterview");
    personEntity.setProperty("when", scheduledInterview.getWhen());
    personEntity.setProperty("date", scheduledInterview.getDate());
    personEntity.setProperty("interviewer", scheduledInterview.getInterviewerEmail());
    personEntity.setProperty("interviewee", scheduledInterview.getIntervieweeEmail());
    return scheduledInterviewEntity;
  }

  /**
   * Retrieve a scheduledInterviewEntity from Datastore
   * and return it as a scheduledInterview object.
   */
  public Optional get(String email) {
    try {
      Optional<Entity> scheduledInterviewEntity = datastore.get(email); 
    } catch (com.google.appengine.api.datastore.EntityNotFoundException e) {
      return Optional.empty(); 
    }
    Filter interviewerFilter = new FilterPredicate("interviewer", FilterOperator.EQUAL, email);
    return scheduledInterviewEntity;
  }

  private static ScheduledInterview entityToScheduledInterview(Entity scheduledInterviewEntity) {
    return new ScheduledInterview(
        (TimeRange) scheduledInterviewEntity.getProperty("when"),
        (LocalDate) scheduledInterviewEntity.getProperty("date"),
        (String) scheduledInterviewEntity.getProperty("interviewer"),
        (String) scheduledInterviewEntity.getProperty("interviewee"));
  }
}