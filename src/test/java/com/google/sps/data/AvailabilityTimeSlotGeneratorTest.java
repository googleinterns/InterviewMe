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

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class AvailabilityTimeSlotGeneratorTest {

  private static final int numberOfSlotsPerDay = 48;

  @Test
  public void createADayOfTimeSlots() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 10, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-4)));
    Instant instant = day.toInstant();
    int timezoneOffsetMinutes = -240;
    List<AvailabilityTimeSlot> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForDay(instant, timezoneOffsetMinutes);

    List<AvailabilityTimeSlot> expected = new ArrayList<AvailabilityTimeSlot>();

    expected.add(AvailabilityTimeSlot.create("2020-07-07T12:00:00Z", "8:00 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T12:15:00Z", "8:15 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T12:30:00Z", "8:30 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T12:45:00Z", "8:45 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T13:00:00Z", "9:00 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T13:15:00Z", "9:15 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T13:30:00Z", "9:30 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T13:45:00Z", "9:45 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T14:00:00Z", "10:00 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T14:15:00Z", "10:15 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T14:30:00Z", "10:30 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T14:45:00Z", "10:45 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T15:00:00Z", "11:00 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T15:15:00Z", "11:15 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T15:30:00Z", "11:30 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T15:45:00Z", "11:45 AM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T16:00:00Z", "12:00 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T16:15:00Z", "12:15 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T16:30:00Z", "12:30 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T16:45:00Z", "12:45 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T17:00:00Z", "1:00 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T17:15:00Z", "1:15 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T17:30:00Z", "1:30 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T17:45:00Z", "1:45 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T18:00:00Z", "2:00 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T18:15:00Z", "2:15 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T18:30:00Z", "2:30 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T18:45:00Z", "2:45 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T19:00:00Z", "3:00 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T19:15:00Z", "3:15 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T19:30:00Z", "3:30 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T19:45:00Z", "3:45 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T20:00:00Z", "4:00 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T20:15:00Z", "4:15 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T20:30:00Z", "4:30 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T20:45:00Z", "4:45 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T21:00:00Z", "5:00 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T21:15:00Z", "5:15 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T21:30:00Z", "5:30 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T21:45:00Z", "5:45 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T22:00:00Z", "6:00 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T22:15:00Z", "6:15 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T22:30:00Z", "6:30 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T22:45:00Z", "6:45 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T23:00:00Z", "7:00 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T23:15:00Z", "7:15 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T23:30:00Z", "7:30 PM", "Tue 7/7", false));
    expected.add(AvailabilityTimeSlot.create("2020-07-07T23:45:00Z", "7:45 PM", "Tue 7/7", false));

    /*
    int[] estHours = {8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7};
    int[] utcHours = {12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    String[] minutes = {"00", "15", "30", "45"};

    for (int i = 0; i < numberOfSlotsPerDay; i++) {
      String utc = "2020-07-07T" + utcHours[i / 4] + ":" + minutes[i % 4] + ":00Z";
      String time;
      if (utcHours[i / 4] < 16) {
        time = estHours[i / 4] + ":" + minutes[i % 4] + " AM";
      } else {
        time = estHours[i / 4] + ":" + minutes[i % 4] + " PM";
      }
      expected.add(AvailabilityTimeSlot.create(utc, time, "Tue 7/7", false));
    } */

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void zeroOffset() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 10, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(0)));
    Instant instant = day.toInstant();
    int timezoneOffsetMinutes = 0;
    List<AvailabilityTimeSlot> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForDay(instant, timezoneOffsetMinutes);
    AvailabilityTimeSlot actualFirstEntry = actual.get(0);

    AvailabilityTimeSlot expectedFirstEntry =
        AvailabilityTimeSlot.create("2020-07-07T08:00:00Z", "8:00 AM", "Tue 7/7", false);

    Assert.assertEquals(expectedFirstEntry, actualFirstEntry);
  }

  @Test
  public void midnightReturnsNextDay() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 0, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-4)));
    Instant instant = day.toInstant();
    int timezoneOffsetMinutes = -240;
    List<AvailabilityTimeSlot> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForDay(instant, timezoneOffsetMinutes);
    AvailabilityTimeSlot actualFirstEntry = actual.get(0);

    AvailabilityTimeSlot expectedFirstEntry =
        AvailabilityTimeSlot.create("2020-07-07T12:00:00Z", "8:00 AM", "Tue 7/7", false);

    Assert.assertEquals(expectedFirstEntry, actualFirstEntry);
  }

  @Test
  public void tooLargePositiveOffset() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          AvailabilityTimeSlotGenerator.timeSlotsForDay(Instant.now(), 740);
        });
  }

  @Test
  public void tooLargeNegativeOffset() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          AvailabilityTimeSlotGenerator.timeSlotsForDay(Instant.now(), -740);
        });
  }
}
