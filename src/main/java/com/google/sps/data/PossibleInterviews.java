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

import com.google.sps.data.AvailabilityDao;
import com.google.sps.data.PersonDao;
import java.time.format.TextStyle;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** 
 * A generator of a list  of PossibleInterview objects within the specified time range
 * and timezone.
 */
public class PossibleInterviews {
  public static List<PossibleInterview> getPossibleInterviews(
      List<Availability> allAvailabilities,
      Instant startOfRange,
      Instant endOfRange,
      ZoneOffset timezoneOffset,
      AvailabilityDao availabilityDao,
      PersonDao personDao) {
    List<PossibleInterview> possibleInterviews = new ArrayList<PossibleInterview>();
    Set<String> interviewers = new HashSet<String>();
    for (Availability avail : allAvailabilities) {
      interviewers.add(avail.email());
    }

    for (String email : interviewers) {
      possibleInterviews.addAll(
          getPossibleInterviewsForPerson(
              email, startOfRange, endOfRange, timezoneOffset, availabilityDao, personDao));
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

  private static List<PossibleInterview> getPossibleInterviewsForPerson(
      String email,
      Instant startOfRange,
      Instant endOfRange,
      ZoneOffset timezoneOffset,
      AvailabilityDao availabilityDao,
      PersonDao personDao) {
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

  private static String getDate(Instant instant, ZoneOffset timezoneOffset) {
    ZonedDateTime day = instant.atZone(ZoneId.ofOffset("UTC", timezoneOffset));
    String dayOfWeek = day.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
    int month = day.getMonthValue();
    int dayOfMonth = day.getDayOfMonth();
    return String.format("%s %d/%d", dayOfWeek, month, dayOfMonth);
  }

  private static String getTime(Instant instant, ZoneOffset timezoneOffset) {
    ZonedDateTime startTime = instant.atZone(ZoneId.ofOffset("UTC", timezoneOffset));
    ZonedDateTime endTime = startTime.plus(1, ChronoUnit.HOURS);
    return String.format("%s - %s", formatTime(startTime), formatTime(endTime));
  }

  private static String formatTime(ZonedDateTime time) {
    int hour = time.getHour();
    int minute = time.getMinute();
    int standardHour = hour;
    if (hour > 12) {
      standardHour = hour - 12;
    }
    return String.format("%d:%02d %s", standardHour, minute, hour < 12 ? "AM" : "PM");
  }
}
