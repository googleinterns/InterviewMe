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
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import java.io.IOException;
import java.io.BufferedReader;
import java.lang.Integer;
import java.time.Instant;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@WebServlet("/search-interviews")
public class SearchInterviewServlet extends HttpServlet {

  private AvailabilityDao availabilityDao;
  private ScheduledInterviewDao scheduledInterviewDao;
  private Instant instant;

  @Override
  public void init() {
    init(new DatastoreAvailabilityDao(), new DatastoreScheduledInterviewDao(), Instant.now());
  }

  public void init(AvailabilityDao availabilityDao, ScheduledInterviewDao scheduledInterviewDao, Instant instant) {
    this.avaialbilityDao = availabilityDao;
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.instant = instant;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int timezoneOffsetMinutes = Instant.parse(request.getParameter("timeZoneOffset"));
    Preconditions.checkArgument(
        Math.abs(timezoneOffsetMinutes) <= 720,
        "Offset greater than 720 minutes (12 hours): %s",
        timezoneOffsetMinutes);
    ZonedDateTime day = generateDay(instant, timezoneOffsetMinutes);
    ZonedDateTime utcTime = day.withZoneSameInstant(ZoneOffset.UTC);
    List<Availability> availabilitiesInRange = avaialbilityDao.
  }
  
  // Uses an Instant and a timezoneOffsetMinutes int to create a ZonedDateTime instance.
  private static ZonedDateTime generateDay(Instant instant, int timezoneOffsetMinutes) {
    return instant.atZone(ZoneId.ofOffset("UTC", convertIntToOffset(timezoneOffsetMinutes)));
  }
  
  // This method takes the timezoneOffsetMinutes int and converts it
  // into a proper ZoneOffset instance.
  private static ZoneOffset convertIntToOffset(int timezoneOffsetMinutes) {
    return ZoneOffset.ofHoursMinutes((timezoneOffsetMinutes / 60), (timezoneOffsetMinutes % 60));
  }
}
