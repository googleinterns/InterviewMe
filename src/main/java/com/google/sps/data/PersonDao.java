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

import java.util.Optional;

/** PersonDao includes the basic methods anything managing Person entities must support. */
public interface PersonDao {
  // Returns a Person object from storage, with email as the key.
  public Optional<Person> get(String email);

  // Adds a Person object into storage, with email as the key.
  public void create(Person person);

  // Updates a Person object in storage, with email as the key.
  public void update(Person person);
}
