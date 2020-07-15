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
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/** Accesses Datastore to support managing ScheduledInterview entities. */
public class FakeScheduledInterviewDao implements ScheduledInterviewDao {
  // @param datastore The DatastoreService we're using to interact with Datastore.
  public Map<String, ScheduledInterview> datastore;

  /** Initializes the fields for ScheduledInterviewDatastoreDAO. */
  public FakeScheduledInterviewDao() {
    datastore = new TreeMap<String, ScheduledInterview>();
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
  /*
  @Override
  public List<ScheduledInterview> getForPerson(String email) {
    List<ScheduledInterview> relevantInterviews = new ArrayList<>();
    List<ScheduledInterview> unsortedInterviews =
        new ArrayList<ScheduledInterview>(datastore.values());
    for (Map.Entry<String, ScheduledInterview> entry : datastore.entrySet()) {
      String key = entry.getKey();
      ScheduledInterview value = entry.getValue();
      if (email.equals(value.interviewerEmail()) || email.equals(value.intervieweeEmail())) {
        relevantInterviews.add(value);
      }
    }
    return relevantInterviews;
  }
  */

  @Override
  public List<ScheduledInterview> getForPerson(String email) {
    List<ScheduledInterview> relevantInterviews = new ArrayList<>();
    List<ScheduledInterview> unsortedInterviews =
        new ArrayList<ScheduledInterview>(datastore.values());
    for (ScheduledInterview scheduledInterview : unsortedInterviews) {
      if (email.equals(scheduledInterview.interviewerEmail())
          || email.equals(scheduledInterview.intervieweeEmail())) {
        relevantInterviews.add(scheduledInterview);
      }
    }
    return relevantInterviews;
  }

  /** Creates a ScheduledInterview Entity and stores it in Datastore. */
  @Override
  public void create(ScheduledInterview scheduledInterview) {
    long generatedId = new Random().nextLong();
    ScheduledInterview storedScheduledInterview =
        ScheduledInterview.create(
            generatedId,
            scheduledInterview.when(),
            scheduledInterview.interviewerEmail(),
            scheduledInterview.intervieweeEmail());
    datastore.put(Long.toString(generatedId), storedScheduledInterview);
  }

  /** Updates an entity in datastore. */
  @Override
  public void update(ScheduledInterview scheduledInterview) {
    datastore.put(Long.toString(scheduledInterview.id()), scheduledInterview);
  }

  /** Deletes an entity in datastore. */
  @Override
  public void delete(long id) {
    datastore.remove(Long.toString(id));
  }
}
