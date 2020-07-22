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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/** Mimics accessing Datastore to support managing Person entities. */
public class FakePersonDao implements PersonDao {
  private HashMap<Long, Person> storedObjects;

  /** Initializes the fields for PersonDatastoreDAO. */
  public FakePersonDao() {
    storedObjects = new HashMap<Long, Person>();
  }

  /** We put person into storedObjects with a randomly generated long as its id. */
  @Override
  public void create(Person person) {
    long id = new Random().nextLong();
    Person toStorePerson = person.withId(id);
    storedObjects.put(id, toStorePerson);
  }

  /** We update person in storedObjects. */
  @Override
  public void update(Person person) {
    storedObjects.put(person.id(), person);
  }

  /**
   * Retrieve the person from storedObjects from their id and wrap it in an Optional. If they aren't
   * in datastore, the Optional is empty.
   */
  @Override
  public Optional<Person> get(long id) {
    if (storedObjects.containsKey(id)) {
      return Optional.of(storedObjects.get(id));
    }
    return Optional.empty();
  }

  /**
   * Retrieve the person from storedObjects from their email and wrap it in an Optional. If they
   * aren't in datastore, the Optional is empty.
   */
  @Override
  public Optional<Person> getFromEmail(String email) {
    List<Person> allPeople = new ArrayList<Person>(storedObjects.values());
    for (Person person : allPeople) {
      if (person.email().equals(email)) {
        return Optional.of(person);
      }
    }
    return Optional.empty();
  }
}
