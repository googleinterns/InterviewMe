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
    String timezoneOffset = "-240";
    List<AvailabilityTimeSlot> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForDay(instant, timezoneOffset);

    List<AvailabilityTimeSlot> expected = new ArrayList<AvailabilityTimeSlot>();

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
    }

    Assert.assertEquals(actual, expected);
  }
}
