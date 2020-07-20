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
import com.google.sps.data.PossibleInterviews;
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
        PossibleInterviews.getPossibleInterviews(
            availabilitiesInRange,
            startOfRange,
            endOfRange,
            timezoneOffset,
            availabilityDao,
            personDao);
    
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
}
