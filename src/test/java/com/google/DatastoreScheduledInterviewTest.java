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

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.TestFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatastoreScheduledInterviewTest {

  DatastoreScheduledInterviewDao tester = new DatastoreScheduledInterviewDao();

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  private final ScheduledInterview s1 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T17:00:10.324978Z"),
              Instant.parse("2020-07-06T18:00:10.324978Z")),
          "user@company.org",
          "user@mail.com");

  private final ScheduledInterview s2 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T19:00:10.324978Z"),
              Instant.parse("2020-07-06T20:00:10.324978Z")),
          "user@company.org",
          "user2@mail.com");

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Test whether the scheduledInterview was added to datastore
  @Test
  public void createsAndStoresEntity() {
    Entity ent = tester.scheduledInterviewToEntity(s1);
    datastore.put(ent);
    assertEquals(1, datastore.prepare(new Query("ScheduledInterview")).countEntities(withLimit(1)));
  }

  // Tests whether all scheduledInterviews for a particular user are returned
  @Test
  public void getsAllScheduledInterviews() {
    Entity ent1 = tester.scheduledInterviewToEntity(s1);
    Entity ent2 = tester.scheduledInterviewToEntity(s2);

    datastore.put(ent1);
    datastore.put(ent2);

    List<ScheduledInterview> result = tester.getForPerson("user@company.org");
    assertEquals(2, result.size());
  }
}
