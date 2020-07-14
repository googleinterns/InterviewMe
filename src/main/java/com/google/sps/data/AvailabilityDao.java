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

import java.util.List;
import java.util.Optional;

/**
 * AvailabilityDao includes the basic methods anything managing Availability entities must support.
 */
public interface AvailabilityDao {
  // Returns a list of all Availabilities ranging from minTime to maxTime of a user.
  // minTime and maxTime are in milliseconds.
  public List<Availability> getInRangeForUser(String email, long minTime, long maxTime);
  
  // Returns a list of all scheduled Availabilities ranging from minTim to maxTime of a 
  // user. minTime and maxTime are in milliseconds.
  public List<Availability> getScheduledInRangeForUser(String email, long minTime, long maxTime);

  // Returns all Availabilities across all users ranging from minTime to maxTime.
  // minTime and maxTime are in milliseconds.
  public List<Availability> getInRangeForAll(long minTime, long maxTime);

  // Returns the Availability entity with specified id.
  public Optional<Availability> get(long id);

  // Adds an Availability object into storage.
  public void create(Availability availability);

  // Updates the specified id with the new availability.
  public void update(Availability availability);

  // Deletes all Availability entities for a user ranging from minTime to maxTime.
  // minTime and maxTime are in milliseconds.
  public void deleteInRangeForUser(String email, long minTime, long maxTime);
}
