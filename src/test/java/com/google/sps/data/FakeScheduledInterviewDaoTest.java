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
  private final FakeScheduledInterviewDao tester = new FakeScheduledInterviewDao();

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

  // Test whether the scheduledInterview was added to datastore.
  @Test
  public void createsScheduledInterview() {
    tester.create(scheduledInterview1);
    List<ScheduledInterview> storedScheduledInterviews =
        new ArrayList<ScheduledInterview>(tester.datastore.values());
    ScheduledInterview expected =
        ScheduledInterview.create(
            storedScheduledInterviews.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
            "user@company.org",
            "user@mail.com");
    Assert.assertEquals(expected, storedScheduledInterviews.get(0));
  }

  // Tests updating a user's scheduledInterview.
  @Test
  public void updatesScheduledInterview() {
    tester.create(scheduledInterview1);
    List<ScheduledInterview> storedScheduledInterviews =
        new ArrayList<ScheduledInterview>(tester.datastore.values());
    ScheduledInterview updatedScheduledInterview =
        ScheduledInterview.create(
            storedScheduledInterviews.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
            "user@company.org",
            "user2@mail.com");
    tester.update(updatedScheduledInterview);
    List<ScheduledInterview> updatedScheduledInterviews =
        new ArrayList<ScheduledInterview>(tester.datastore.values());
    Assert.assertEquals(updatedScheduledInterview, updatedScheduledInterviews.get(0));
  }

  // Tests retrieving a scheduledInterview from Datastore.
  @Test
  public void getsScheduledInterview() {
    tester.create(scheduledInterview1);
    List<ScheduledInterview> storedScheduledInterviews =
        new ArrayList<ScheduledInterview>(tester.datastore.values());
    ScheduledInterview expected =
        ScheduledInterview.create(
            storedScheduledInterviews.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
            "user@company.org",
            "user@mail.com");
    Assert.assertEquals(Optional.of(expected), tester.get(expected.id()));
  }

  // Tests retrieving a scheduledInterview that doesn't exist from Datastore.
  @Test
  public void failsToGetScheduledInterview() {
    Optional<ScheduledInterview> actual = tester.get(24);
    Optional<ScheduledInterview> expected = Optional.empty();
    Assert.assertEquals(expected, actual);
  }
  
  // Tests deleting a user's scheduledInterview.
  tester.create(scheduledInterview1); 
  tester.create(scheduledInterview2); 
  List<ScheduledInterview> result; 
}
