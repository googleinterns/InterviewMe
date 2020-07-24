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
import com.google.sps.data.PossibleInterviewer;
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
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.util.Optional;

@WebServlet("/show-interviewers")
public class ShowInterviewersServlet extends HttpServlet {

  private AvailabilityDao availabilityDao;
  private PersonDao personDao;

  @Override
  public void init() {
    init(new DatastoreAvailabilityDao(), new DatastorePersonDao());
  }

  public void init(AvailabilityDao availabilityDao, PersonDao personDao) {
    this.availabilityDao = availabilityDao;
    this.personDao = personDao;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String utc = request.getParameter("utc");
    Instant startOfRange = Instant.parse(utc);
    Instant endOfRange = Instant.parse(utc).plus(1, ChronoUnit.HOURS);
    List<Availability> availabilitiesInRange =
        availabilityDao.getInRangeForAll(startOfRange, endOfRange);
    List<Person> possiblePeople =
        getPossiblePeople(availabilitiesInRange, startOfRange, endOfRange);
    Set<PossibleInterviewer> possibleInterviewers = getPossibleInterviewers(possiblePeople);
    request.setAttribute("interviewers", possibleInterviewers);
    RequestDispatcher rd = request.getRequestDispatcher("/possibleInterviewers.jsp");
    rd.forward(request, response);
  }

  List<Person> getPossiblePeople(
      List<Availability> allAvailabilities, Instant startOfRange, Instant endOfRange) {
    Set<String> allInterviewers = new HashSet<String>();
    for (Availability avail : allAvailabilities) {
      allInterviewers.add(avail.userId());
    }

    // We don't want to schedule an interview for a user with themself, so we are removing
    // the current user's id from the list.
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    String userId = userService.getCurrentUser().getUserId();

    // Since UserId does not have a valid Mock, if the id is null (as when testing), it will be
    // replaced with this hashcode.
    if (userId == null) {
      userId = String.format("%d", userEmail.hashCode());
    }

    allInterviewers.remove(userId);

    List<Person> possibleInterviewers = new ArrayList<Person>();
    for (String interviewer : allInterviewers) {
      if (personHasPossibleInterviewSlot(interviewer, startOfRange, endOfRange)) {
        possibleInterviewers.add(personDao.get(interviewer).get());
      }
    }

    return possibleInterviewers;
  }

  private Set<PossibleInterviewer> getPossibleInterviewers(List<Person> possiblePeople) {
    Set<PossibleInterviewer> possibleInterviewers = new HashSet<PossibleInterviewer>();
    for (Person person : possiblePeople) {
      possibleInterviewers.add(personToPossibleInterviewer(person));
    }

    return possibleInterviewers;
  }

  boolean personHasPossibleInterviewSlot(String userId, Instant startOfRange, Instant endOfRange) {
    List<Availability> availabilities =
        availabilityDao.getInRangeForUser(userId, startOfRange, endOfRange);
    List<Availability> scheduledAvailability = new ArrayList<Availability>();
    for (Availability avail : availabilities) {
      if (avail.scheduled()) {
        scheduledAvailability.add(avail);
      }
    }
    availabilities.removeAll(scheduledAvailability);
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

  private PossibleInterviewer personToPossibleInterviewer(Person person) {
    return PossibleInterviewer.create(person.company(), person.job());
  }
}
