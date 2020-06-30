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

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.time.ZoneId;

/**
 * A collection of AvailabilityTimeSlot Objects, which will eventually be generated with information
 * from datastore.
 */
public class AvailabilityTimeSlots {
  private List<AvailabilityTimeSlot> timeSlots = new ArrayList<AvailabilityTimeSlot>();

  public AvailabilityTimeSlots() {
    System.out.println(Instant.now().atZone(ZoneId.systemDefault()).getDayOfWeek().toString());
    List<String> dates = generateDates();
    List<String> times = generateTimes();
  }

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
}
