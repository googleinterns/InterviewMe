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

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.sps.data.Availability;
import com.google.sps.data.AvailabilityDao;
import com.google.sps.data.DatastoreAvailabilityDao;
import com.google.sps.data.DatastorePersonDao;
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.Person;
import com.google.sps.data.PersonDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.TimeRange;
import java.io.IOException;
import java.io.BufferedReader;
import java.lang.Integer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@WebServlet("/select-interview")
public class SelectInterviewServlet extends HttpServlet {

  private AvailabilityDao availabilityDao;
  private ScheduledInterviewDao scheduledInterviewDao;
  private PersonDao personDao;

  @Override
  public void init() {
    init(
        new DatastoreAvailabilityDao(),
        new DatastoreScheduledInterviewDao(),
        new DatastorePersonDao());
  }

  public void init(
      AvailabilityDao availabilityDao,
      ScheduledInterviewDao scheduledInterviewDao,
      PersonDao personDao) {
    this.availabilityDao = availabilityDao;
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.personDao = personDao;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String utc = request.getParameter("utc");
    Instant startOfRange = Instant.parse(utc);
    Instant endOfRange = Instant.parse(utc).plus(1, ChronoUnit.HOURS);
    List<Availability> availabilitiesInRange =
        availabilityDao.getInRangeForAll(startOfRange, endOfRange);
    List<Person> possibleInterviewers =
        getPossibleInterviewers(availabilitiesInRange, startOfRange, endOfRange);
    // TODO: Send this to modal jsp with the utc (to be able to schedule the interview).
  }

  private List<Person> getPossibleInterviewers(
      List<Availability> allAvailabilities, Instant startOfRange, Instant endOfRange) {
    Set<String> allInterviewers = new HashSet<String>();
    for (Availability avail : allAvailabilities) {
      allInterviewers.add(avail.email());
    }

    List<Person> possibleInterviewers = new ArrayList<Person>();
    for (String email : allInterviewers) {
      if (personHasPossibleInterviewSlot(email, startOfRange, endOfRange)) {
        possibleInterviewers.add(personDao.get(email).get());
      }
    }

    return possibleInterviewers;
  }

  private boolean personHasPossibleInterviewSlot(
      String email, Instant startOfRange, Instant endOfRange) {
    List<Availability> availabilities =
        availabilityDao.getInRangeForUser(email, startOfRange, endOfRange);
    for (int i = 0; i < availabilities.size() - 3; i++) {
      if (availabilities.get(i).when().end().equals(availabilities.get(i + 1).when().start())) {
        if (availabilities
            .get(i + 1)
            .when()
            .end()
            .equals(availabilities.get(i + 2).when().start())) {
          if (availabilities
              .get(i + 2)
              .when()
              .end()
              .equals(availabilities.get(i + 3).when().start())) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
