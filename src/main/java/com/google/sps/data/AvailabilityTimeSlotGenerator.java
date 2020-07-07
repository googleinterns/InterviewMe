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
import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.time.format.DateTimeFormatter;

// TODO: Add a test for the constructor.

/**
 * A collection of AvailabilityTimeSlot Objects, which will eventually be generated with information
 * from datastore.
 */
public class AvailabilityTimeSlotGenerator {
  private List<AvailabilityTimeSlot> timeSlots = new ArrayList<AvailabilityTimeSlot>();

  /** Constructs an AvailabilityTimeSlotGenerator object by generating the timeSlots list. */
  public AvailabilityTimeSlotGenerator(String timezoneOffset) {
    ZoneOffset timeZoneOffset = convertStringToOffset(timezoneOffset);
    ZoneId zoneId = ZoneId.ofOffset("UTC", timeZoneOffset);
    ZonedDateTime today = Instant.now().atZone(zoneId);
    String dayOfWeek = today.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US);
    int year = today.getYear();
    int month = today.getMonthValue();
    int dayOfMonth = today.getDayOfMonth();

    // TODO: Generate a week of slots.
    String date = generateDate(dayOfWeek, month, dayOfMonth);
    List<String> times = generateTimes();
    List<String> utcEncodings = generateUTCEncodings(year, month, dayOfMonth, zoneId);
    List<Boolean> selectedStatuses = getSelectedStatuses();
    timeSlots = generateTimeSlots(utcEncodings, times, date, selectedStatuses);
  }

  private ZoneOffset convertStringToOffset(String timezoneOffset) {
    int offsetTotalMinutes = Integer.parseInt(timezoneOffset);
    int offsetHours = offsetTotalMinutes / 60;
    int offsetMinutes = offsetTotalMinutes % 60;
    return ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes);
  }

  private String generateDate(String dayOfWeek, int month, int dayOfMonth) {
    return dayOfWeek + " " + month + "/" + dayOfMonth;
  }

  private List<String> generateUTCEncodings(int year, int month, int dayOfMonth, ZoneId zoneId) {
    List<String> utcEncodings = new ArrayList<String>();
    int[] minutes = {0, 15, 30, 45};
    for (int i = 8; i < 20; i++) {
      for (int j = 0; j < 4; j++) {
        LocalDateTime localTimeSlot = LocalDateTime.of(year, month, dayOfMonth, i, minutes[j]);
        ZonedDateTime utcTimeSlot =
            ZonedDateTime.of(localTimeSlot, zoneId).withZoneSameInstant(ZoneOffset.UTC);
        String formattedUTCTimeSlot = utcTimeSlot.format(DateTimeFormatter.ISO_INSTANT);
        utcEncodings.add(formattedUTCTimeSlot);
      }
    }
    return utcEncodings;
  }

  // TODO: Access the time slots from data store to tell if they are selected or not.
  private List<Boolean> getSelectedStatuses() {
    List<Boolean> selectedStatuses = new ArrayList<Boolean>();
    for (int i = 0; i < 48; i++) {
      selectedStatuses.add(false);
    }
    return selectedStatuses;
  }

  private List<AvailabilityTimeSlot> generateTimeSlots(
      List<String> utcEncodings, List<String> times, String date, List<Boolean> selectedStatuses) {
    List<AvailabilityTimeSlot> availabilityTimeSlots = new ArrayList<AvailabilityTimeSlot>();
    for (int i = 0; i < 48; i++) {
      availabilityTimeSlots.add(
          AvailabilityTimeSlot.create(
              utcEncodings.get(i), times.get(i), date, selectedStatuses.get(i)));
    }
    return availabilityTimeSlots;
  }

  // TODO: Change to generate current week with calls to generateDate
  private List<String> generateDates() {
    List<String> dates = new ArrayList<String>();
    dates.add("Sunday 6/28");
    dates.add("Monday 6/29");
    dates.add("Tuesday 6/30");
    dates.add("Wednesday 7/1");
    dates.add("Thursday 7/2");
    dates.add("Friday 7/3");
    dates.add("Saturday 7/4");
    return dates;
  }

  private List<String> generateTimes() {
    List<String> times = new ArrayList<String>();
    for (int i = 8; i < 12; i++) {
      times.add(i + ":00 AM");
      times.add(i + ":15 AM");
      times.add(i + ":30 AM");
      times.add(i + ":45 AM");
    }
    times.add("12:00 PM");
    times.add("12:15 PM");
    times.add("12:30 PM");
    times.add("12:45 PM");
    for (int i = 1; i < 8; i++) {
      times.add(i + ":00 PM");
      times.add(i + ":15 PM");
      times.add(i + ":30 PM");
      times.add(i + ":45 PM");
    }
    return times;
  }

  public List<AvailabilityTimeSlot> getTimeSlots() {
    return timeSlots;
  }
}
