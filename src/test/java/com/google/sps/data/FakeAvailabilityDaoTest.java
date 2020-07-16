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
  private final FakeAvailabilityDao dao = new FakeAvailabilityDao();

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
    dao.create(availabilityOne);
    List<Availability> storedAvailabilities = new ArrayList<Availability>(dao.datastore.values());
    Availability expected =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
            storedAvailabilities.get(0).id(),
            true);
    Assert.assertEquals(expected, storedAvailabilities.get(0));
  }

  // Checks that an Availability is updated in datastore.
  @Test
  public void updatesAvailability() {
    dao.create(availabilityOne);
    List<Availability> storedAvailabilities = new ArrayList<Availability>(dao.datastore.values());
    Availability update =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
            storedAvailabilities.get(0).id(),
            false);
    dao.update(update);
    List<Availability> updatedAvailabilities = new ArrayList<Availability>(dao.datastore.values());
    Assert.assertEquals(update, updatedAvailabilities.get(0));
  }

  // Checks that an Availability is returned when it exists within datastore.
  @Test
  public void getsAvailability() {
    dao.create(availabilityOne);
    List<Availability> storedAvailabilities = new ArrayList<Availability>(dao.datastore.values());
    Availability expected =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
            storedAvailabilities.get(0).id(),
            true);
    Assert.assertEquals(Optional.of(expected), dao.get(expected.id()));
  }

  // Checks that an empty Optional is returned when an Availability does not exist within
  // datastore.
  @Test
  public void failsToGetAvailability() {
    Optional<Availability> actual = dao.get(24);
    Optional<Availability> expected = Optional.empty();
    Assert.assertEquals(expected, actual);
  }

  // Checks that the Availability objects within a given time range for a specified user
  // are deleted.
  @Test
  public void deletesInRange() {
    dao.create(availabilityOne);
    dao.create(availabilityTwo);
    dao.create(availabilityFour);
    dao.deleteInRangeForUser(
        "user1@mail.com",
        Instant.parse("2020-07-07T12:00:00Z").toEpochMilli(),
        Instant.parse("2020-07-07T16:00:00Z").toEpochMilli());
    List<Availability> storedAvailabilities = new ArrayList<Availability>(dao.datastore.values());
    Availability expected =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T22:30:00Z"), Instant.parse("2020-07-07T22:45:00Z")),
            storedAvailabilities.get(0).id(),
            true);
    Assert.assertEquals(expected, storedAvailabilities.get(0));
  }

  // Checks that only the Availability objects for the specified user are deleted within
  // a given time range (and not the Availability objects of other users).
  @Test
  public void deletesUsersAvailabilityInRange() {
    dao.create(availabilityOne);
    dao.create(availabilityTwo);
    dao.create(availabilityThree);
    dao.deleteInRangeForUser(
        "user1@mail.com",
        Instant.parse("2020-07-07T12:00:00Z").toEpochMilli(),
        Instant.parse("2020-07-07T17:45:00Z").toEpochMilli());
    List<Availability> storedAvailabilities = new ArrayList<Availability>(dao.datastore.values());
    Availability expected =
        Availability.create(
            "user2@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T17:30:00Z"), Instant.parse("2020-07-07T17:45:00Z")),
            storedAvailabilities.get(0).id(),
            true);
    Assert.assertEquals(expected, storedAvailabilities.get(0));
  }

  // Checks that all Availability objects for a user within a given time range are returned.
  @Test
  public void getsUsersAvailabilityInRange() {
    dao.create(availabilityOne);
    dao.create(availabilityTwo);
    dao.create(availabilityFour);
    List<Availability> actual =
        dao.getInRangeForUser(
            "user1@mail.com",
            Instant.parse("2020-07-07T12:00:00Z").toEpochMilli(),
            Instant.parse("2020-07-07T16:00:00Z").toEpochMilli());
    List<Availability> storedAvailabilities = new ArrayList<Availability>(dao.datastore.values());
    Availability expectedAvailabilityOne =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
            storedAvailabilities.get(0).id(),
            true);
    Availability expectedAvailabilityTwo =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T15:45:00Z"), Instant.parse("2020-07-07T16:00:00Z")),
            storedAvailabilities.get(1).id(),
            false);
    List<Availability> expectedAvailabilities = new ArrayList<Availability>();
    expectedAvailabilities.add(expectedAvailabilityOne);
    expectedAvailabilities.add(expectedAvailabilityTwo);
    Assert.assertEquals(expectedAvailabilities, actual);
  }

  // Checks that only the Availability objects for the specified user are returned within
  // a given time range (and not the Availability objects of other users).
  @Test
  public void onlyGetsSpecifiedUsersAvailabilityInRange() {
    dao.create(availabilityOne);
    dao.create(availabilityTwo);
    dao.create(availabilityThree);
    List<Availability> actual =
        dao.getInRangeForUser(
            "user1@mail.com",
            Instant.parse("2020-07-07T12:00:00Z").toEpochMilli(),
            Instant.parse("2020-07-07T17:45:00Z").toEpochMilli());
    List<Availability> storedAvailabilities = new ArrayList<Availability>(dao.datastore.values());
    Availability expectedAvailabilityOne =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
            storedAvailabilities.get(0).id(),
            true);
    Availability expectedAvailabilityTwo =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T15:45:00Z"), Instant.parse("2020-07-07T16:00:00Z")),
            storedAvailabilities.get(1).id(),
            false);
    List<Availability> expectedAvailabilities = new ArrayList<Availability>();
    expectedAvailabilities.add(expectedAvailabilityOne);
    expectedAvailabilities.add(expectedAvailabilityTwo);
    Assert.assertEquals(expectedAvailabilities, actual);
  }

  // Checks that all of the Availability objects within a given time range are returned.
  @Test
  public void getsAllUsersAvailabilityInRange() {
    dao.create(availabilityOne);
    dao.create(availabilityTwo);
    dao.create(availabilityThree);
    dao.create(availabilityFour);
    List<Availability> actual =
        dao.getInRangeForAll(
            Instant.parse("2020-07-07T12:00:00Z").toEpochMilli(),
            Instant.parse("2020-07-07T17:45:00Z").toEpochMilli());
    List<Availability> storedAvailabilities = new ArrayList<Availability>(dao.datastore.values());
    Availability expectedAvailabilityOne =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
            storedAvailabilities.get(0).id(),
            true);
    Availability expectedAvailabilityTwo =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T15:45:00Z"), Instant.parse("2020-07-07T16:00:00Z")),
            storedAvailabilities.get(1).id(),
            false);
    Availability expectedAvailabilityThree =
        Availability.create(
            "user2@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T17:30:00Z"), Instant.parse("2020-07-07T17:45:00Z")),
            storedAvailabilities.get(2).id(),
            true);
    List<Availability> expectedAvailabilities = new ArrayList<Availability>();
    expectedAvailabilities.add(expectedAvailabilityOne);
    expectedAvailabilities.add(expectedAvailabilityTwo);
    expectedAvailabilities.add(expectedAvailabilityThree);
    Assert.assertEquals(expectedAvailabilities, actual);
  }
}
