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
import static org.junit.Assert.assertEquals;

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

public class DatastoreScheduledInterviewTest {

  DatastoreScheduledInterviewDao tester = new DatastoreScheduledInterviewDao();

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  private final ScheduledInterview scheduledInterview1 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
          "user@company.org",
          "user@mail.com");

  private final ScheduledInterview scheduledInterview2 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
          "user@company.org",
          "user2@mail.com");
  private final ScheduledInterview scheduledInterview3 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
          "user3@company.org",
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

  // Test whether the scheduledInterview was added to datastore.
  @Test
  public void createsAndStoresEntity() {
    tester.create(scheduledInterview1);
    Entity entity = datastore.prepare(new Query("ScheduledInterview")).asSingleEntity();
    ScheduledInterview storedScheduledInterview = tester.entityToScheduledInterview(entity);
    ScheduledInterview copyScheduledInterview1 =
        ScheduledInterview.create(
            storedScheduledInterview.id(),
            new TimeRange(
                Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
            "user@company.org",
            "user@mail.com");
    assertEquals(storedScheduledInterview.id(), copyScheduledInterview1.id());
  }

  // Tests whether all scheduledInterviews for a particular user are returned.
  @Test
  public void getsAllScheduledInterviews() {
    tester.create(scheduledInterview1);
    tester.create(scheduledInterview2);
    tester.create(scheduledInterview3);
    List<ScheduledInterview> result = tester.getForPerson("user@company.org");
    ScheduledInterview copyScheduledInterview1 =
        ScheduledInterview.create(
            result.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
            "user@company.org",
            "user@mail.com");
    ScheduledInterview copyScheduledInterview2 =
        ScheduledInterview.create(
            result.get(1).id(),
            new TimeRange(
                Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
            "user@company.org",
            "user2@mail.com");

    assertEquals(2, result.size());
    assertEquals(copyScheduledInterview1.interviewerEmail(), result.get(0).interviewerEmail());
    assertEquals(copyScheduledInterview2.interviewerEmail(), result.get(1).interviewerEmail());
  }

  // Tests deleting a user's scheduledInterview.
  @Test
  public void deletesScheduledInterview() {
    tester.create(scheduledInterview1);
    tester.create(scheduledInterview2);
    List<ScheduledInterview> result = tester.getForPerson("user@company.org");
    tester.delete(result.get(0).id());
    Entity entity = datastore.prepare(new Query("ScheduledInterview")).asSingleEntity();
    ScheduledInterview storedScheduledInterview = tester.entityToScheduledInterview(entity);
    ScheduledInterview copyScheduledInterview2 =
        ScheduledInterview.create(
            storedScheduledInterview.id(),
            new TimeRange(
                Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
            "user@company.org",
            "user2@mail.com");
    assertEquals(storedScheduledInterview, copyScheduledInterview2);
  }

  // Tests updating a user's scheduledInterview.
  @Test
  public void updatesScheduledInterview() {
    tester.create(scheduledInterview1);
    Entity entity = datastore.prepare(new Query("ScheduledInterview")).asSingleEntity();
    ScheduledInterview storedScheduledInterview = tester.entityToScheduledInterview(entity);
    ScheduledInterview updatedScheduledInterview =
        ScheduledInterview.create(
            storedScheduledInterview.id(),
            new TimeRange(
                Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
            "user@company.org",
            "user3@mail.com");
    tester.update(storedScheduledInterview);
    assertEquals(false, storedScheduledInterview.equals(updatedScheduledInterview));
  }
}
