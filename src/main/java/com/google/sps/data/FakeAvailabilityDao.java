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

import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/** Mimics accessing Datastore to support managing Availability entities. */
public class FakeAvailabilityDao implements AvailabilityDao {
  // @param datastore the fake database. Made public because it will only be used
  // while testing.
  public LinkedHashMap<Long, Availability> datastore;

  /** Initializes the fields for FakeAvailabilityDao. */
  public FakeAvailabilityDao() {
    datastore = new LinkedHashMap<Long, Availability>();
  }

  /** Puts an Availability object into datastore with a randomly generated long as its id. */
  @Override
  public void create(Availability avail) {
    long id = new Random().nextLong();
    Availability toStoreAvail = avail.withId(id);
    datastore.put(id, toStoreAvail);
  }

  /** Updates an Availability in datastore based on its id. */
  @Override
  public void update(Availability avail) {
    datastore.put(avail.id(), avail);
  }

  /**
   * Retrieves the Availability from datastore from the given id and wraps it in an Optional. If the
   * Availability does not exist in datastore, the Optional is empty.
   */
  @Override
  public Optional<Availability> get(long id) {
    if (datastore.containsKey(id)) {
      return Optional.of(datastore.get(id));
    }
    return Optional.empty();
  }

  /**
   * Deletes all Availability entities for a user ranging from minTime to maxTime. minTime and
   * maxTime are in milliseconds.
   */
  @Override
  public void deleteInRangeForUser(String email, long minTime, long maxTime) {
    List<Availability> userAvailability = getForUser(email);
    List<Availability> userAvailabilityInRange = getInRange(userAvailability, minTime, maxTime);
    for (Availability avail : userAvailabilityInRange) {
      datastore.remove(avail.id());
    }
  }

  /**
   * Collects all Availabilities for the specified user within the specified time range, where
   * minTime and maxTime are in milliseconds.
   */
  @Override
  public List<Availability> getInRangeForUser(String email, long minTime, long maxTime) {
    List<Availability> userAvailability = getForUser(email);
    return getInRange(userAvailability, minTime, maxTime);
  }

  /**
   * Collects all Availabilities for the specified user within the specified time range that have
   * been scheduled over, where minTime and maxTime are in milliseconds.
   */
  @Override
  public List<Availability> getScheduledInRangeForUser(String email, long minTime, long maxTime) {
    List<Availability> userAvailability = getForUser(email);
    List<Availability> userAvailabilityInRange = getInRange(userAvailability, minTime, maxTime);
    return getScheduled(userAvailabilityInRange);
  }

  private List<Availability> getForUser(String email) {
    List<Availability> allAvailability = new ArrayList<Availability>(datastore.values());
    List<Availability> userAvailability = new ArrayList<Availability>();
    for (Availability avail : allAvailability) {
      if (avail.email().equals(email)) {
        userAvailability.add(avail);
      }
    }
    return userAvailability;
  }

  private List<Availability> getScheduled(List<Availability> allAvailability) {
    List<Availability> scheduledAvailability = new ArrayList<Availability>();
    for (Availability avail : allAvailability) {
      if (avail.scheduled()) {
        scheduledAvailability.add(avail);
      }
    }
    return scheduledAvailability;
  }

  /**
   * Collects all Availabilities within the specified time range, where minTime and maxTime are in
   * milliseconds.
   */
  @Override
  public List<Availability> getInRangeForAll(long minTime, long maxTime) {
    return getInRange(new ArrayList<Availability>(datastore.values()), minTime, maxTime);
  }

  private List<Availability> getInRange(
      List<Availability> allAvailability, long minTime, long maxTime) {
    TimeRange range = new TimeRange(Instant.ofEpochMilli(minTime), Instant.ofEpochMilli(maxTime));
    List<Availability> inRangeAvailability = new ArrayList<Availability>();
    for (Availability avail : allAvailability) {
      if (range.contains(avail.when())) {
        inRangeAvailability.add(avail);
      }
    }
    return inRangeAvailability;
  }
}
