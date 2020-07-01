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

package com.google.sps;

/**
 * ScheduledInterviewDao includes the basic methods anything managing ScheduledInterview entities
 * must support.
 */
public interface ScheduledInterviewDao {
  // Returns a the most first ScheduledInterview object from storage using the users email as the key.
  public ScheduledInterview get(String email) throws com.google.appengine.api.datastore.EntityNotFoundException;

  // Puts a ScheduledInterview object into storage.
  public void put(ScheduledInterview scheduledInterview);

  // Creates a ScheduledInterview entity from scheduledInterview object. 
  public void create(); 

  // Updates a ScheduledInterview object.
  public void update(ScheduledInterview updatedScheduledInterview);
}
