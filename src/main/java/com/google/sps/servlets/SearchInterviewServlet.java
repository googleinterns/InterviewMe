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
import com.google.sps.data.PersonDao;
import com.google.sps.data.PossibleInterview;
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

@WebServlet("/search-interviews")
public class SearchInterviewServlet extends HttpServlet {

  private AvailabilityDao availabilityDao;
  private ScheduledInterviewDao scheduledInterviewDao;
  private PersonDao personDao;
  private Instant instant;

  @Override
  public void init() {
    init(
        new DatastoreAvailabilityDao(),
        new DatastoreScheduledInterviewDao(),
        new DatastorePersonDao(),
        Instant.now());
  }

  public void init(
      AvailabilityDao availabilityDao,
      ScheduledInterviewDao scheduledInterviewDao,
      PersonDao personDao,
      Instant instant) {
    this.availabilityDao = availabilityDao;
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.personDao = personDao;
    this.instant = instant;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("DEBUG: Makes it to doGet in Servlet.");
    int timezoneOffsetMinutes = Integer.parseInt(request.getParameter("timeZoneOffset"));
    Preconditions.checkArgument(
        Math.abs(timezoneOffsetMinutes) <= 720,
        "Offset greater than 720 minutes (12 hours): %s",
        timezoneOffsetMinutes);
    ZoneOffset timezoneOffset = convertIntToOffset(timezoneOffsetMinutes);
    ZonedDateTime day = generateDay(instant, timezoneOffset);
    ZonedDateTime utcTime = day.withZoneSameInstant(ZoneOffset.UTC);
    // TODO: Decide how precise this range should be. Must start after or at now, when should end
    // be?
    Instant startOfRange = utcTime.toInstant();
    Instant endOfRange = utcTime.toInstant().plus(6, ChronoUnit.DAYS);
    List<Availability> availabilitiesInRange =
        availabilityDao.getInRangeForAll(startOfRange, endOfRange);
    List<PossibleInterview> possibleInterviews =
        getPossibleInterviews(availabilitiesInRange, startOfRange, endOfRange, timezoneOffset);
  }

  // Uses an Instant and a timezoneOffset to create a ZonedDateTime instance.
  private static ZonedDateTime generateDay(Instant instant, ZoneOffset timezoneOffset) {
    return instant.atZone(ZoneId.ofOffset("UTC", timezoneOffset));
  }

  // This method takes the timezoneOffsetMinutes int and converts it
  // into a proper ZoneOffset instance.
  private static ZoneOffset convertIntToOffset(int timezoneOffsetMinutes) {
    return ZoneOffset.ofHoursMinutes((timezoneOffsetMinutes / 60), (timezoneOffsetMinutes % 60));
  }

  private List<PossibleInterview> getPossibleInterviews(
      List<Availability> allAvailabilities,
      Instant startOfRange,
      Instant endOfRange,
      ZoneOffset timezoneOffset) {
    List<PossibleInterview> possibleInterviews = new ArrayList<PossibleInterview>();
    Set<String> interviewers = new HashSet<String>();
    for (Availability avail : allAvailabilities) {
      interviewers.add(avail.email());
    }

    for (String email : interviewers) {
      possibleInterviews.addAll(
          getPossibleInterviewsForPerson(email, startOfRange, endOfRange, timezoneOffset));
    }

    possibleInterviews.sort(
        (PossibleInterview p1, PossibleInterview p2) -> {
          if (Instant.parse(p1.utcEncoding()).equals(Instant.parse(p2.utcEncoding()))) {
            return 0;
          }
          if (Instant.parse(p1.utcEncoding()).isBefore(Instant.parse(p2.utcEncoding()))) {
            return -1;
          }
          return 1;
        });
    return possibleInterviews;
  }

  private List<PossibleInterview> getPossibleInterviewsForPerson(
      String email, Instant startOfRange, Instant endOfRange, ZoneOffset timezoneOffset) {
    List<Availability> availabilities =
        availabilityDao.getInRangeForUser(email, startOfRange, endOfRange);
    List<PossibleInterview> possibleInterviewsForPerson = new ArrayList<PossibleInterview>();
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
            possibleInterviewsForPerson.add(
                PossibleInterview.create(
                    // TODO: Deal with empty Optional case
                    personDao.get(email).get(),
                    availabilities.get(0).when().start().toString(),
                    getDate(availabilities.get(0).when().start(), timezoneOffset),
                    getTime(availabilities.get(0).when().start(), timezoneOffset)));
          }
        }
      }
    }
    return possibleInterviewsForPerson;
  }

  private String getDate(Instant instant, ZoneOffset timezoneOffset) {
    ZonedDateTime day = instant.atZone(ZoneId.ofOffset("UTC", timezoneOffset));
    String dayOfWeek = day.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
    int month = day.getMonthValue();
    int dayOfMonth = day.getDayOfMonth();
    return String.format("%s %d/%d", dayOfWeek, month, dayOfMonth);
  }

  private String getTime(Instant instant, ZoneOffset timezoneOffset) {
    ZonedDateTime startTime = instant.atZone(ZoneId.ofOffset("UTC", timezoneOffset));
    ZonedDateTime endTime = startTime.plus(1, ChronoUnit.HOURS);
    return String.format("%s - %s", formatTime(startTime), formatTime(endTime));
  }

  private String formatTime(ZonedDateTime time) {
    int hour = time.getHour();
    int minute = time.getMinute();
    int standardHour = hour;
    if (hour > 12) {
      standardHour = hour - 12;
    }
    return String.format("%d:%02d %s", standardHour, minute, hour < 12 ? "AM" : "PM");
  }
}