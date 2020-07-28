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
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.PossibleInterviewSlot;
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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.util.Optional;

@WebServlet("/load-interviews")
public class LoadInterviewsServlet extends HttpServlet {

  private AvailabilityDao availabilityDao;
  private ScheduledInterviewDao scheduledInterviewDao;
  private Instant instant;

  @Override
  public void init() {
    init(new DatastoreAvailabilityDao(), new DatastoreScheduledInterviewDao(), Instant.now());
  }

  public void init(
      AvailabilityDao availabilityDao,
      ScheduledInterviewDao scheduledInterviewDao,
      Instant instant) {
    this.availabilityDao = availabilityDao;
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.instant = instant;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
    TimeRange range =
        new TimeRange(utcTime.toInstant(), utcTime.toInstant().plus(6, ChronoUnit.DAYS));
    List<Availability> availabilitiesInRange =
        availabilityDao.getInRangeForAll(range.start(), range.end());
    List<PossibleInterviewSlot> possibleInterviews =
        getPossibleInterviewSlots(availabilitiesInRange, range, timezoneOffset);

    String date = "";
    List<ArrayList<PossibleInterviewSlot>> possibleInterviewsForWeek =
        new ArrayList<ArrayList<PossibleInterviewSlot>>();

    if (possibleInterviews.size() != 0) {
      date = possibleInterviews.get(0).date();
      ArrayList<PossibleInterviewSlot> dayOfSlots = new ArrayList<PossibleInterviewSlot>();
      for (PossibleInterviewSlot possibleInterview : possibleInterviews) {
        if (!possibleInterview.date().equals(date)) {
          possibleInterviewsForWeek.add(dayOfSlots);
          dayOfSlots = new ArrayList<PossibleInterviewSlot>();
          date = possibleInterview.date();
        }
        dayOfSlots.add(possibleInterview);
      }
      possibleInterviewsForWeek.add(dayOfSlots);
    }

    request.setAttribute("weekList", possibleInterviewsForWeek);
    RequestDispatcher rd = request.getRequestDispatcher("/possibleInterviewTimes.jsp");

    try {
      rd.forward(request, response);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
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
      List<Availability> allAvailabilities, TimeRange range, ZoneOffset timezoneOffset) {
    Set<PossibleInterviewSlot> possibleInterviews = new HashSet<PossibleInterviewSlot>();
    Set<String> interviewers = new HashSet<String>();
    for (Availability avail : allAvailabilities) {
      interviewers.add(avail.userId());
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

    interviewers.remove(userId);

    for (String interviewer : interviewers) {
      possibleInterviews.addAll(
          getPossibleInterviewSlotsForPerson(interviewer, range, timezoneOffset));
    }

    // We need to check that the person looking to schedule is not already scheduled during any of
    // the proposed times.

    List<ScheduledInterview> userScheduledInterviews =
        scheduledInterviewDao.getScheduledInterviewsInRangeForUser(
            userId, range.start(), range.end());

    Set<PossibleInterviewSlot> conflictingInterviews = new HashSet<PossibleInterviewSlot>();

    for (PossibleInterviewSlot slot : possibleInterviews) {
      for (ScheduledInterview userInterview : userScheduledInterviews) {
        if (userInterview.when().contains(Instant.parse(slot.utcEncoding()))) {
          conflictingInterviews.add(slot);
        }
      }
    }

    possibleInterviews.removeAll(conflictingInterviews);

    List<PossibleInterviewSlot> possibleInterviewList =
        new ArrayList<PossibleInterviewSlot>(possibleInterviews);

    sortInterviews(possibleInterviewList);
    return possibleInterviewList;
  }

  private List<PossibleInterviewSlot> getPossibleInterviewSlotsForPerson(
      String userId, TimeRange range, ZoneOffset timezoneOffset) {
    List<Availability> availabilities =
        availabilityDao.getInRangeForUser(userId, range.start(), range.end());
    availabilities.removeIf(avail -> avail.scheduled());

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

  private void sortWeek(List<ArrayList<PossibleInterviewSlot>> possibleInterviewsForWeek) {
    possibleInterviewsForWeek.sort(
        (ArrayList<PossibleInterviewSlot> d1, ArrayList<PossibleInterviewSlot> d2) -> {
          if (Instant.parse(d1.get(0).utcEncoding())
              .equals(Instant.parse(d2.get(0).utcEncoding()))) {
            return 0;
          }
          if (Instant.parse(d1.get(0).utcEncoding())
              .isBefore(Instant.parse(d2.get(0).utcEncoding()))) {
            return -1;
          }
          return 1;
        });
  }
}
