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

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.time.format.DateTimeFormatter;

/** A generator of a collection of AvailabilityTimeSlot Objects. */
public class AvailabilityTimeSlotGenerator {
  private static final int EARLIEST_HOUR = 8;
  private static final int LATEST_HOUR = 19;
  // A list of hours and minutes representing permitted time slots.
  private List<HourAndMinute> candidateHoursAndMinutes;
  private static int numberOfSlotsPerDay;

  @AutoValue
  abstract static class HourAndMinute {
    abstract int hour();

    abstract int minute();

    static HourAndMinute create(int hour, int minute) {
      return builder().setHour(hour).setMinute(minute).build();
    }

    static Builder builder() {
      return new AutoValue_AvailabilityTimeSlotGenerator_HourAndMinute.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
      abstract Builder setHour(int hour);

      abstract Builder setMinute(int minute);

      abstract HourAndMinute build();
    }
  }

  public void init() {
    candidateHoursAndMinutes = new ArrayList<HourAndMinute>();
    for (int i = EARLIEST_HOUR; i < LATEST_HOUR + 1; i++) {
      candidateHoursAndMinutes.add(HourAndMinute.create(i, 0));
      candidateHoursAndMinutes.add(HourAndMinute.create(i, 15));
      candidateHoursAndMinutes.add(HourAndMinute.create(i, 30));
      candidateHoursAndMinutes.add(HourAndMinute.create(i, 45));
    }

    numberOfSlotsPerDay = candidateHoursAndMinutes.size();
  }

  /**
   * Constructs a list of a day's worth of AvailabilityTimeSlot objects.
   *
   * @param instant An instant on the day that time slots are generated for.
   * @param timezoneOffsetMinutes An int that represents the difference between UTC and the user's
   *     current timezone. Example: A user in EST has a timezoneOffsetMinutes of -240 which means
   *     that EST is 240 minutes behind UTC.
   * @throws IllegalArgumentException if the magnitude of timezoneOffsetMinutes is greater than 720.
   */
  // TODO: Create a timeSlotsForWeek method.
  public static List<AvailabilityTimeSlot> timeSlotsForDay(
      Instant instant, int timezoneOffsetMinutes) {
    Preconditions.checkArgument(
        Math.abs(timezoneOffsetMinutes) <= 720,
        "Offset greater than 720 minutes (12 hours): %s",
        timezoneOffsetMinutes);
    ZonedDateTime day = generateDay(instant, timezoneOffsetMinutes);
    ZoneId zoneId = day.getZone();
    String dayOfWeek = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US);
    int year = day.getYear();
    int month = day.getMonthValue();
    int dayOfMonth = day.getDayOfMonth();

    String date = formatDate(dayOfWeek, month, dayOfMonth);
    List<String> times = availableStartTimes();
    int numberOfSlotsPerDay = times.size();
    List<String> utcEncodings = generateUTCEncodings(year, month, dayOfMonth, zoneId);
    List<Boolean> selectedStatuses = getSelectedStatuses(numberOfSlotsPerDay, utcEncodings);
    return generateTimeSlots(numberOfSlotsPerDay, utcEncodings, times, date, selectedStatuses);
  }

  // Uses an Instant and a timezoneOffsetMinutes int to create a ZonedDateTime instance
  // for the day specified by the Instant.
  private static ZonedDateTime generateDay(Instant instant, int timezoneOffsetMinutes) {
    return instant.atZone(ZoneId.ofOffset("UTC", convertIntToOffset(timezoneOffsetMinutes)));
  }

  // This method takes the timezoneOffsetMinutes int and converts it
  // into a proper ZoneOffset instance.
  private static ZoneOffset convertIntToOffset(int timezoneOffsetMinutes) {
    return ZoneOffset.ofHoursMinutes((timezoneOffsetMinutes / 60), (timezoneOffsetMinutes % 60));
  }

  // Returns a readable date string such as "Tue 7/7".
  private static String formatDate(String dayOfWeek, int month, int dayOfMonth) {
    return String.format("%s %d/%d", dayOfWeek, month, dayOfMonth);
  }

  // This method returns a list of UTC Strings for a single day, creating one String per Time
  // Slot.
  private static List<String> generateUTCEncodings(
      int year, int month, int dayOfMonth, ZoneId zoneId) {
    List<String> utcEncodings = new ArrayList<String>();
    int[] minutes = {0, 15, 30, 45};
    for (int i = EARLIEST_HOUR; i <= LATEST_HOUR; i++) {
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

  // TODO: Access the time slots from data store using the utcEncondings to tell if they
  // are selected or not. Can get rid of numberOfSlotsPerDay upon new implementation.
  // This method will tell whether or not a time slot has already been selected. (See
  // TODO above).
  private static List<Boolean> getSelectedStatuses(
      int numberOfSlotsPerDay, List<String> utcEncodings) {
    List<Boolean> selectedStatuses = new ArrayList<Boolean>();
    for (int i = 0; i < numberOfSlotsPerDay; i++) {
      selectedStatuses.add(false);
    }
    return selectedStatuses;
  }

  // Returns a list of readable time Strings such as "8:00 AM".
  private static List<String> availableStartTimes() {
    Integer[] validHours = new Integer[LATEST_HOUR - EARLIEST_HOUR + 1];
    for (int i = EARLIEST_HOUR; i < LATEST_HOUR + 1; i++) {
      validHours[i - EARLIEST_HOUR] = i;
    }
    ImmutableList<Integer> supportedHours = ImmutableList.copyOf(validHours);
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

  // This method combines the lists of previously generated information into a list of constructed
  // AvailabilityTimeSlot objects.
  private static List<AvailabilityTimeSlot> generateTimeSlots(
      int numberOfSlotsPerDay,
      List<String> utcEncodings,
      List<String> times,
      String date,
      List<Boolean> selectedStatuses) {
    List<AvailabilityTimeSlot> availabilityTimeSlots = new ArrayList<AvailabilityTimeSlot>();
    for (int i = 0; i < numberOfSlotsPerDay; i++) {
      availabilityTimeSlots.add(
          AvailabilityTimeSlot.create(
              utcEncodings.get(i), times.get(i), date, selectedStatuses.get(i)));
    }
    return availabilityTimeSlots;
  }
}
