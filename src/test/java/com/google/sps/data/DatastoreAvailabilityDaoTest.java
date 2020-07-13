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
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;

@RunWith(JUnit4.class)
public class DatastoreAvailabilityDaoTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private final DatastoreAvailabilityDao tester = new DatastoreAvailabilityDao();
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void createsAvailability() {
    tester.create(availabilityOne);
    Entity entity = datastore.prepare(new Query("Availability")).asSingleEntity();
    Availability storedAvailability = tester.entityToAvailability(entity);
    Availability availabilityOneWithID =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
            storedAvailability.id(),
            true);
    Assert.assertEquals(availabilityOneWithID, storedAvailability);
  }

  @Test
  public void updatesAvailability() {
    tester.create(availabilityOne);
    Entity entity = datastore.prepare(new Query("Availability")).asSingleEntity();
    Availability storedAvailability = tester.entityToAvailability(entity);
    Availability update =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
            storedAvailability.id(),
            false);
    tester.update(update);
    Entity updatedEntity = datastore.prepare(new Query("Availability")).asSingleEntity();
    Availability updatedAvailability = tester.entityToAvailability(updatedEntity);
    Availability updateWithID =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
            updatedAvailability.id(),
            false);
    Assert.assertEquals(updateWithID, updatedAvailability);
  }

  @Test
  public void getsAvailability() {
    tester.create(availabilityTwo);
    Entity entity = datastore.prepare(new Query("Availability")).asSingleEntity();
    Availability storedAvailability = tester.entityToAvailability(entity);
    Optional<Availability> actualAvailabilityOptional = tester.get(storedAvailability.id());
    Availability expectedAvailability =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T15:45:00Z"), Instant.parse("2020-07-07T16:00:00Z")),
            storedAvailability.id(),
            false);
    Optional<Availability> expectedAvailabilityOptional = Optional.of(expectedAvailability);
    Assert.assertEquals(expectedAvailabilityOptional, actualAvailabilityOptional);
  }

  @Test
  public void failsToGetAvailability() {
    Optional<Availability> actual = tester.get(24);
    Optional<Availability> expected = Optional.empty();
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void deletesUsersAvailabilityInRange() {
    tester.create(availabilityOne);
    tester.create(availabilityTwo);
    tester.create(availabilityFour);
    tester.deleteInRangeForUser(
        "user1@mail.com",
        Instant.parse("2020-07-07T12:00:00Z").toEpochMilli(),
        Instant.parse("2020-07-07T16:00:00Z").toEpochMilli());
    Entity entity = datastore.prepare(new Query("Availability")).asSingleEntity();
    Availability actual = tester.entityToAvailability(entity);
    Availability expected =
        Availability.create(
            "user1@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T22:30:00Z"), Instant.parse("2020-07-07T22:45:00Z")),
            actual.id(),
            true);
    Assert.assertEquals(expected, actual);
  }
}
