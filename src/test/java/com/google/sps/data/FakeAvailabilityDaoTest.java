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
public class FakeAvailabilityDaoTest {
  private final FakeAvailabilityDao tester = new FakeAvailabilityDao();

  private final Availability availabilityOne =
      Availability.create(
          "user1@mail.com",
          new TimeRange(
              Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
          (long) -1,
          true);

  private final Availability availabilityTwo =
      Availability.create(
          "user1@mail.com",
          new TimeRange(
              Instant.parse("2020-07-07T15:45:00Z"), Instant.parse("2020-07-07T16:00:00Z")),
          (long) -1,
          false);

  private final Availability availabilityThree =
      Availability.create(
          "user2@mail.com",
          new TimeRange(
              Instant.parse("2020-07-07T17:30:00Z"), Instant.parse("2020-07-07T17:45:00Z")),
          (long) -1,
          true);

  private final Availability availabilityFour =
      Availability.create(
          "user1@mail.com",
          new TimeRange(
              Instant.parse("2020-07-07T22:30:00Z"), Instant.parse("2020-07-07T22:45:00Z")),
          (long) -1,
          true);

  // Checks that an Availability is stored in datastore.
  @Test
  public void createsAvailability() {
    // tester.create(availabilityOne);
    // Assert.assertTrue(tester.datastore.contains(availabilityOne));
  }

  // Checks that an Availability is updated in datastore.
  @Test
  public void updatesAvailability() {}

  // Checks that an Availability is returned when it exists within datastore.
  @Test
  public void getsAvailability() {}

  // Checks that an empty Optional is returned when an Availability does not exist within
  // datastore.
  @Test
  public void failsToGetAvailability() {}

  // Checks that the Availability objects within a given time range for a specified user
  // are deleted.
  @Test
  public void deletesInRange() {}

  // Checks that only the Availability objects for the specified user are deleted within
  // a given time range (and not the Availability objects of other users).
  @Test
  public void deletesUsersAvailabilityInRange() {}

  // Checks that all Availability objects for a user within a given time range are returned.
  @Test
  public void getsUsersAvailabilityInRange() {}

  // Checks that only the Availability objects for the specified user are returned within
  // a given time range (and not the Availability objects of other users).
  @Test
  public void onlyGetsSpecifiedUsersAvailabilityInRange() {}

  // Checks that all scheduled Availability objects from a specified user
  // and within a given time range are returned.
  @Test
  public void getsScheduledAvailabilitiesForUserInRange() {}

  // Checks that only the scheduled Availability objects for a specific user
  // are returned (and not the scheduled Availability objects of other users).
  @Test
  public void onlyGetsSpecifiedUsersScheduledAvailabilityInRange() {}

  // Checks that all of the Availability objects within a given time range are returned.
  @Test
  public void getsAllUsersAvailabilityInRange() {}
}
