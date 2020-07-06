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

/**
 * An AvailabilityTimeSlot represents a table entry on the availability page that has a date and
 * time (utcEncoding), a visual represenation of the time (timeString), and a class that is set
 * based on whether or not the slot has already been selected in a previous visit to the page (green
 * = selected = 'table-success').
 */
@AutoValue
public abstract class AvailabilityTimeSlot {
  abstract String utcEncoding();

  abstract String time();

  abstract String date();

  abstract boolean selected();

  public static AvailabilityTimeSlot create(
      String utcEncoding, String time, String date, boolean selected) {
    return builder()
        .setUtcEncoding(utcEncoding)
        .setTime(time)
        .setDate(date)
        .setSelected(selected)
        .build();
  }

  static Builder builder() {
    return new AutoValue_AvailabilityTimeSlot.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setUtcEncoding(String utcEncoding);

    abstract Builder setTime(String time);

    abstract Builder setDate(String date);

    abstract Builder setSelected(boolean selected);

    abstract AvailabilityTimeSlot build();
  }
}

/**
 * public class AvailabilityTimeSlot { private String utcEncoding; private String time; private
 * String date; private boolean selected;
 *
 * <p>/** This constructor creates a new Availability object public AvailabilityTimeSlot(String
 * utcEncoding, String time, String date, boolean selected) { this.utcEncoding = utcEncoding;
 * this.time = time; this.date = date; this.selected = selected; }
 *
 * <p>public String getUTCEncoding() { return utcEncoding; }
 *
 * <p>public String getTime() { return time; }
 *
 * <p>public String getDate() { return date; }
 *
 * <p>public boolean getSelected() { return selected; } }
 */
