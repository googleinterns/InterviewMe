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

import com.google.common.collect.ImmutableList;
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

/** A generator of a collection of AvailabilityTimeSlot Objects. */
public class AvailabilityTimeSlotGenerator {
  private static final int numberOfSlotsPerDay = 48;
  private static final int TIME_8AM = 8;
  private static final int TIME_7PM = 19;

  /**
   * Constructs a list of a day's worth of AvailabilityTimeSlot objects.
   *
   * @param instant An instant on the day that time slots are generated for.
   * @param timezoneOffset A String that represents the difference between UTC and the user's
   *     current timezone. Example: A user in EST has a timezoneOffset of "-240" which means that
   *     EST is 240 minutes behind UTC.
   */
  // TODO: Create a timeSlotsForWeek method.
  public static List<AvailabilityTimeSlot> timeSlotsForDay(Instant instant, String timezoneOffset) {
    ZonedDateTime day = generateDay(instant, timezoneOffset);
    ZoneId zoneId = day.getZone();
    String dayOfWeek = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US);
    int year = day.getYear();
    int month = day.getMonthValue();
    int dayOfMonth = day.getDayOfMonth();

    String date = generateDate(dayOfWeek, month, dayOfMonth);
    List<String> times = availableStartTimes();
    List<String> utcEncodings = generateUTCEncodings(year, month, dayOfMonth, zoneId);
    List<Boolean> selectedStatuses = getSelectedStatuses();
    return generateTimeSlots(utcEncodings, times, date, selectedStatuses);
  }

  // Uses an Instant and a timezoneOffset String to create a ZonedDateTime instance
  // for the day specified by the Instant.
  private static ZonedDateTime generateDay(Instant instant, String timezoneOffset) {
    return instant.atZone(ZoneId.ofOffset("UTC", convertStringToOffset(timezoneOffset)));
  }

  // This method takes the timezoneOffset String and converts it
  // into a proper ZoneOffset instance.
  private static ZoneOffset convertStringToOffset(String timezoneOffset) {
    int offsetTotalMinutes = Integer.parseInt(timezoneOffset);
    int offsetHours = offsetTotalMinutes / 60;
    int offsetMinutes = offsetTotalMinutes % 60;
    return ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes);
  }

  // Returns a readable date string such as "Tue 7/7".
  private static String generateDate(String dayOfWeek, int month, int dayOfMonth) {
    return dayOfWeek + " " + month + "/" + dayOfMonth;
  }

  // This method returns a list of UTC Strings for a single day, creating one String per Time
  // Slot.
  private static List<String> generateUTCEncodings(
      int year, int month, int dayOfMonth, ZoneId zoneId) {
    List<String> utcEncodings = new ArrayList<String>();
    int[] minutes = {0, 15, 30, 45};
    for (int i = TIME_8AM; i <= TIME_7PM; i++) {
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
  // This method will tell whether or not a time slot has already been selected. (See
  // TODO above).
  private static List<Boolean> getSelectedStatuses() {
    List<Boolean> selectedStatuses = new ArrayList<Boolean>();
    for (int i = 0; i < numberOfSlotsPerDay; i++) {
      selectedStatuses.add(false);
    }
    return selectedStatuses;
  }

  // This method combines the lists of previously generated information into a list of constructed
  // AvailabilityTimeSlot objects.
  private static List<AvailabilityTimeSlot> generateTimeSlots(
      List<String> utcEncodings, List<String> times, String date, List<Boolean> selectedStatuses) {
    List<AvailabilityTimeSlot> availabilityTimeSlots = new ArrayList<AvailabilityTimeSlot>();
    for (int i = 0; i < numberOfSlotsPerDay; i++) {
      availabilityTimeSlots.add(
          AvailabilityTimeSlot.create(
              utcEncodings.get(i), times.get(i), date, selectedStatuses.get(i)));
    }
    return availabilityTimeSlots;
  }

  // Returns a list of readable time Strings such as "8:00 AM".
  private static List<String> availableStartTimes() {
    ImmutableList<Integer> supportedHours =
        ImmutableList.of(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
    ImmutableList<Integer> supportedMinutes = ImmutableList.of(0, 15, 30, 45);
    ImmutableList.Builder<String> timesBuilder = ImmutableList.builder();
    for (Integer hour : supportedHours) {
      for (Integer minutes : supportedMinutes) {
        int standardHour = hour;
        if (hour > 12) {
          standardHour = hour - 12;
        }
        timesBuilder.add(
            String.format("%d:%02d %s", standardHour, minutes, hour < 12 ? "AM" : "PM"));
      }
    }
    return timesBuilder.build();
  }
}
