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

import java.util.HashMap;
import java.util.Optional;

/** Accesses Datastore to support managing Person entities. */
public class FakePersonDao implements PersonDao {
  // @param datastore stores all data
  private HashMap<String, Person> datastore;

  /** Initializes the fields for PersonDatastoreDAO. */
  public FakePersonDao() {
    datastore = new HashMap<String, Person>();
  }

  /**
   * We make an entity in Datastore with person's fields as properties and their email as the key.
   */
  @Override
  public void put(Person person) {
    datastore.put(person.email(), person);
  }

  /**
   * Retrieve the person from datastore from their email and wrap it in an Optional. If they aren't
   * in datastore, the Optional is empty.
   */
  @Override
  public Optional<Person> get(String email) {
    if (datastore.containsKey(email)) {
      return Optional.of(datastore.get(email));
    }
    return Optional.empty();
  }
}
