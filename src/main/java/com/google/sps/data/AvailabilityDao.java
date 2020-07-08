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

import java.time.ZonedDateTime;
import java.util.List;

/**
 * AvailabilityDao includes the basic methods anything managing Interviewer Availability entities must support.
 */
public interface AvailabilityDao {
  // Returns a list of all Availability's ranging from minTime to maxTime of the person with email
  // as their email.
  public List<Availability> get(String email, ZonedDateTime minTime, ZonedDateTime maxTime);

  // Returns all Availability's across all users ranging from minTime to maxTime.
  public List<Availability> get(ZonedDateTime minTime, ZonedDateTime maxTime);

  // Puts an Availability object into storage.
  public void put(String email, ZonedDateTime time);

  // Deletes an Availability object from storage if present. If not, does nothing.
  public void delete(String email, ZonedDateTime time);
}
