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
import java.util.Optional;
import java.util.List;

/**
 * ScheduledInterviewDao includes the basic methods anything managing ScheduledInterview entities
 * must support.
 */
public interface ScheduledInterviewDao {
  // Returns the ScheduledInterview object.
  public Optional<ScheduledInterview> get(long id);

  // Returns a list, sorted by start time, of all scheduled ScheduledInterview objects ranging from
  // minTime to maxTime. minTime and maxTime are in milliseconds.
  public List<ScheduledInterview> getInRange(Instant minTime, Instant maxTime);

  // Returns a list, sorted by start time, of the ScheduledInterview objects that the user
  // participates in.
  public List<ScheduledInterview> getForPerson(String email);

  // Returns a list, sorted by start time, of all scheduled ScheduledInterview objects ranging from
  // minTime to maxTime of a user. minTime and maxTime are in milliseconds.
  public List<ScheduledInterview> getScheduledInterviewsInRangeForUser(
      String email, Instant minTime, Instant maxTime);

  // Creates a ScheduledInterview entity.
  public void create(ScheduledInterview scheduledInterview);

  // Updates a ScheduledInterview.
  public void update(ScheduledInterview scheduledInterview);

  // Deletes a ScheduledInterview entity.
  public void delete(long id);
}
