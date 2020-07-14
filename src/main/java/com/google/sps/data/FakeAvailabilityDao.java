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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/** Mimics accessing Datastore to support managing Availability entities. */
public class FakeAvailabilityDao implements AvailabilityDao {
  // @param datastore the fake database.
  private HashMap<Long, Availability> datastore;

  /** Initializes the fields for FakeAvailabilityDao. */
  public FakeAvailabilityDao() {
    datastore = new HashMap<Long, Availability>();
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

  @Override
  public void deleteInRangeForUser(String email, long minTime, long maxTime) {}

  @Override
  public List<Availability> getInRangeForUser(String email, long minTime, long maxTime) {
    return Arrays.asList();
  }

  @Override
  public List<Availability> getScheduledInRangeForUser(String email, long minTime, long maxTime) {
    return Arrays.asList();
  }

  @Override
  public List<Availability> getInRangeForAll(long minTime, long maxTime) {
    return Arrays.asList();
  }
}
