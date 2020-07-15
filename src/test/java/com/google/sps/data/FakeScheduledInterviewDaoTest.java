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

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;

@RunWith(JUnit4.class)
public class FakeScheduledInterviewDaoTest {
  private final FakeScheduledInterviewDao dao = new FakeScheduledInterviewDao();

  private final ScheduledInterview scheduledInterview1 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
          "user@company.org",
          "user@gmail.com");

  private final ScheduledInterview scheduledInterview2 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
          "user@company.org",
          "user2@gmail.com");

  private final ScheduledInterview scheduledInterview3 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
          "user3@company.org",
          "user2@gmail.com");

  // Test whether the scheduledInterview was added to datastore.
  @Test
  public void createsScheduledInterview() {
    dao.create(scheduledInterview1);
    List<ScheduledInterview> storedScheduledInterviews =
        new ArrayList<ScheduledInterview>(dao.datastore.values());
    ScheduledInterview expected =
        ScheduledInterview.create(
            storedScheduledInterviews.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
            "user@company.org",
            "user@gmail.com");
    Assert.assertEquals(expected, storedScheduledInterviews.get(0));
  }

  // Tests updating a user's scheduledInterview.
  @Test
  public void updatesScheduledInterview() {
    dao.create(scheduledInterview1);
    List<ScheduledInterview> storedScheduledInterviews =
        new ArrayList<ScheduledInterview>(dao.datastore.values());
    ScheduledInterview updatedScheduledInterview =
        ScheduledInterview.create(
            storedScheduledInterviews.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
            "user@company.org",
            "user2@gmail.com");
    dao.update(updatedScheduledInterview);
    List<ScheduledInterview> updatedScheduledInterviews =
        new ArrayList<ScheduledInterview>(dao.datastore.values());
    Assert.assertEquals(updatedScheduledInterview, updatedScheduledInterviews.get(0));
  }

  // Tests retrieving a scheduledInterview from Datastore.
  @Test
  public void getsScheduledInterview() {
    dao.create(scheduledInterview1);
    List<ScheduledInterview> storedScheduledInterviews =
        new ArrayList<ScheduledInterview>(dao.datastore.values());
    ScheduledInterview expected =
        ScheduledInterview.create(
            storedScheduledInterviews.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
            "user@company.org",
            "user@gmail.com");
    Assert.assertEquals(Optional.of(expected), dao.get(expected.id()));
  }

  // Tests retrieving a scheduledInterview that doesn't exist from Datastore.
  @Test
  public void failsToGetScheduledInterview() {
    Optional<ScheduledInterview> actual = dao.get(24);
    Optional<ScheduledInterview> expected = Optional.empty();
    Assert.assertEquals(expected, actual);
  }

  // Tests retrieving scheduledInterviews in the order in which they occur
  @Test
  public void sortedScheduledInterviews() {
    dao.create(scheduledInterview2);
    dao.create(scheduledInterview1);
    List<ScheduledInterview> actual = dao.getForPerson("user@company.org");
    List<ScheduledInterview> expected = new ArrayList<ScheduledInterview>();
    ScheduledInterview storedScheduledInterview1 =
        ScheduledInterview.create(
            actual.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
            "user@company.org",
            "user@gmail.com");
    ScheduledInterview storedScheduledInterview2 =
        ScheduledInterview.create(
            actual.get(1).id(),
            new TimeRange(
                Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
            "user@company.org",
            "user2@gmail.com");
    expected.add(storedScheduledInterview1);
    expected.add(storedScheduledInterview2);
    Assert.assertEquals(expected, actual);
  }
}
