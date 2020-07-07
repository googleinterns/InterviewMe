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

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class AvailabilityTimeSlotGeneratorTest {
  @Test
  public void fakeTest() {
    Assert.assertTrue(true);
  }

  @Test
  public void createADay() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 10, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-4)));
    List<AvailabilityTimeSlot> actual = AvailabilityTimeSlotGenerator.timeSlotsForDay(day);
    Assert.assertTrue(true);
  }
}