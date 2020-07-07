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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.nio.file.DirectoryStream.Filter;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

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
  public Optional<ScheduledInterview> get(long id) {
    Key key = KeyFactory.createKey("id", id);
    Entity scheduledInterviewEntity; 
    try {
      scheduledInterviewEntity = datastore.get(key); 
    } catch (Exception e) {
      return Optional.empty(); 
    }
  
    return Optional.of(entityToScheduledInterview(scheduledInterviewEntity));
  }

  public List<ScheduledInterview> getForPerson(String email) {
    FilterPredicate interviewerFilter = new FilterPredicate("interviewer", FilterOperator.EQUAL, email);
    FilterPredicate intervieweeFilter = new FilterPredicate("interviewee", FilterOperator.EQUAL, email);
    CompositeFilter compositeFilter = CompositeFilterOperator.and(interviewerFilter, intervieweeFilter);
    Query q = new Query().setFilter(compositeFilter);
    return null;
  }


  /**
   * Updates an entity in datastore.
   */
  public void update(ScheduledInterview scheduledInterview) {
    datastore.put(create(scheduledInterview));
  }

  /**
   * Creates a ScheduledInterview Entity.
   */
  public Entity create(ScheduledInterview scheduledInterview) {
    Entity scheduledInterviewEntity = new Entity("ScheduledInterview");
    scheduledInterviewEntity.setProperty("when", scheduledInterview.getWhen());
    scheduledInterviewEntity.setProperty("date", scheduledInterview.getDate());
    scheduledInterviewEntity.setProperty("interviewer", scheduledInterview.getInterviewerEmail());
    scheduledInterviewEntity.setProperty("interviewee", scheduledInterview.getIntervieweeEmail());
    return scheduledInterviewEntity;
  }

  /** Creates a ScheduledInterview object from a datastore entity */
  public ScheduledInterview entityToScheduledInterview(Entity scheduledInterviewEntity) {
    return new ScheduledInterview(
        (TimeRange) scheduledInterviewEntity.getProperty("when"),
        (LocalDate) scheduledInterviewEntity.getProperty("date"),
        (String) scheduledInterviewEntity.getProperty("interviewer"),
        (String) scheduledInterviewEntity.getProperty("interviewee"));
  }
}
