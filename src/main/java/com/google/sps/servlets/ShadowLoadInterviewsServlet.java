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
import com.google.sps.data.DatastorePersonDao;
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.Job;
import com.google.sps.data.PersonDao;
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
import java.util.EnumSet;
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

@WebServlet("/shadow-load-interviews")
public class ShadowLoadInterviewsServlet extends HttpServlet {
  private ScheduledInterviewDao scheduledInterviewDao;
  private PersonDao personDao;
  private Instant currentTime;
  private final int maxTimezoneOffsetMinutes = 720;
  private final int maxTimezoneOffsetHours = 12;

  @Override
  public void init() {
    init(new DatastoreScheduledInterviewDao(), new DatastorePersonDao(), Instant.now());
  }

  public void init(
      ScheduledInterviewDao scheduledInterviewDao, PersonDao personDao, Instant currentTime) {
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.personDao = personDao;
    this.currentTime = currentTime;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int timezoneOffsetMinutes = Integer.parseInt(request.getParameter("timeZoneOffset"));
    Preconditions.checkArgument(
        Math.abs(timezoneOffsetMinutes) <= maxTimezoneOffsetMinutes,
        "Offset greater than %d minutes (%d hours): %d",
        maxTimezoneOffsetMinutes,
        maxTimezoneOffsetHours,
        timezoneOffsetMinutes);
    ZoneOffset timezoneOffset = convertIntToOffset(timezoneOffsetMinutes);
    ZonedDateTime day = generateDay(currentTime, timezoneOffset);
    ZonedDateTime utcTime = day.withZoneSameInstant(ZoneOffset.UTC);
    // The user will be shown available interview times for the next four weeks, starting from the
    // current time.
    TimeRange interviewSearchTimeRange =
        new TimeRange(utcTime.toInstant(), utcTime.toInstant().plus(27, ChronoUnit.DAYS));
    String position = request.getParameter("position");
    Job selectedPosition = Job.valueOf(Job.class, position);
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    String userId = userService.getCurrentUser().getUserId();
    // Since Users returned from the LocalUserService (in tests) do not have userIds, here we set
    // the userId equal to a hashcode.
    if (userId == null) {
      userId = String.format("%d", userEmail.hashCode());
    }

    List<ScheduledInterview> possibleInterviews = scheduledInterviewDao.getForPositionWithoutShadowInRange(
      userId, selctedPosition, interviewSearchTimeRange.start(), interviewSearchTimeRange.end());

    possibleInterviews.removeIf(
        interview ->
            !personDao.get(interview.intervieweeId()).get().okShadow()
                || !personDao.get(interview.interviewerId()).get().okShadow());
    List<PossibleInterviewSlot> possibleInterviewSlots =
        scheduledInterviewsToPossibleInterviewSlots(possibleInterviews);

    String date = possibleInterviews.isEmpty() ? "" : possibleInterviews.get(0).date();
    List<ArrayList<PossibleInterviewSlot>> possibleInterviewsForWeek =
        new ArrayList<ArrayList<PossibleInterviewSlot>>();

    if (!possibleInterviews.isEmpty()) {
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

  // Converts the timezoneOffsetMinutes int into a proper ZoneOffset instance.
  private static ZoneOffset convertIntToOffset(int timezoneOffsetMinutes) {
    return ZoneOffset.ofHoursMinutes((timezoneOffsetMinutes / 60), (timezoneOffsetMinutes % 60));
  }

  private List<PossibleInterviewSlot> scheduledInterviewsToPossibleInterviewSlots(
      List<ScheduledInterview> interviews, ZoneOffset timezoneOffset) {
    List<PossibleInterviewSlot> possibleInterviewSlots = new ArrayList<PossibleInterviewSlot>();
    for (ScheduledInterview interview : interviews) {
      possibleInterviewSlots.add(
          PossibleInterviewSlot.create(
              interview.when().start().toString(),
              getDate(interview.when().start(), timezoneOffset),
              getTime(interview.when().start(), timezoneOffset)));
    }
    return possibleInterviewSlots;
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
