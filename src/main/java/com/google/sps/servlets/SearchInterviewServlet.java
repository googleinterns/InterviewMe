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
import com.google.common.collect.ImmutableList;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.sps.data.Availability;
import com.google.sps.data.AvailabilityDao;
import com.google.sps.data.DatastoreAvailabilityDao;
import com.google.sps.data.PossibleInterviewSlot;
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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.util.Optional;

@WebServlet("/search-interviews")
public class SearchInterviewServlet extends HttpServlet {

  private AvailabilityDao availabilityDao;
  private Instant instant;

  @Override
  public void init() {
    init(new DatastoreAvailabilityDao(), Instant.now());
  }

  public void init(AvailabilityDao availabilityDao, Instant instant) {
    this.availabilityDao = availabilityDao;
    this.instant = instant;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
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
    List<Availability> scheduledAvailability = new ArrayList<Availability>();
    for (Availability avail : availabilitiesInRange) {
      if (avail.scheduled()) {
        scheduledAvailability.add(avail);
      }
    }
    availabilitiesInRange.removeAll(scheduledAvailability);
    List<PossibleInterviewSlot> possibleInterviews =
        getPossibleInterviewSlots(availabilitiesInRange, startOfRange, endOfRange, timezoneOffset);
    Set<String> dates = new HashSet<String>();
    for (PossibleInterviewSlot pi : possibleInterviews) {
      dates.add(pi.date());
    }

    ImmutableList.Builder<List<PossibleInterviewSlot>> weekList = ImmutableList.builder();
    for (String date : dates) {
      List<PossibleInterviewSlot> dayOfSlots = new ArrayList<PossibleInterviewSlot>();
      for (PossibleInterviewSlot pi : possibleInterviews) {
        if (date.equals(pi.date())) {
          dayOfSlots.add(pi);
        }
      }
      sortInterviews(dayOfSlots);
      weekList.add(dayOfSlots);
    }
    List<List<PossibleInterviewSlot>> possibleInterviewsForWeek = weekList.build();
    request.setAttribute("weekList", possibleInterviewsForWeek);
    List<List<PossibleInterviewSlot>> result =
        (List<List<PossibleInterviewSlot>>) request.getAttribute("weekList");
    RequestDispatcher rd = request.getRequestDispatcher("/possibleInterviewTimes.jsp");
    rd.forward(request, response);
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

  private List<PossibleInterviewSlot> getPossibleInterviewSlots(
      List<Availability> allAvailabilities,
      Instant startOfRange,
      Instant endOfRange,
      ZoneOffset timezoneOffset) {
    Set<PossibleInterviewSlot> possibleInterviews = new HashSet<PossibleInterviewSlot>();
    Set<String> interviewers = new HashSet<String>();
    for (Availability avail : allAvailabilities) {
      interviewers.add(avail.email());
    }

    // We don't want to schedule an interview for a user with themself, so we are removing
    // the current user's email from the list.
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    interviewers.remove(userEmail);

    for (String email : interviewers) {
      possibleInterviews.addAll(
          getPossibleInterviewSlotsForPerson(email, startOfRange, endOfRange, timezoneOffset));
    }

    List<PossibleInterviewSlot> possibleInterviewList =
        new ArrayList<PossibleInterviewSlot>(possibleInterviews);

    sortInterviews(possibleInterviewList);
    return possibleInterviewList;
  }

  private List<PossibleInterviewSlot> getPossibleInterviewSlotsForPerson(
      String email, Instant startOfRange, Instant endOfRange, ZoneOffset timezoneOffset) {
    List<Availability> availabilities =
        availabilityDao.getInRangeForUser(email, startOfRange, endOfRange);
    List<PossibleInterviewSlot> possibleInterviewSlotsForPerson =
        new ArrayList<PossibleInterviewSlot>();
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
            possibleInterviewSlotsForPerson.add(
                PossibleInterviewSlot.create(
                    availabilities.get(i).when().start().toString(),
                    getDate(availabilities.get(i).when().start(), timezoneOffset),
                    getTime(availabilities.get(i).when().start(), timezoneOffset)));
          }
        }
      }
    }
    return possibleInterviewSlotsForPerson;
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

  private void sortInterviews(List<PossibleInterviewSlot> possibleInterviewSlots) {
    possibleInterviewSlots.sort(
        (PossibleInterviewSlot p1, PossibleInterviewSlot p2) -> {
          if (Instant.parse(p1.utcEncoding()).equals(Instant.parse(p2.utcEncoding()))) {
            return 0;
          }
          if (Instant.parse(p1.utcEncoding()).isBefore(Instant.parse(p2.utcEncoding()))) {
            return -1;
          }
          return 1;
        });
  }
}
