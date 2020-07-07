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
import java.time.LocalDate;

/**
 * Availability is a time range when a given person is available to offer interviews as an
 * interviewer.
 */
@AutoValue
public abstract class Availability {
  abstract String email();

  abstract TimeRange when();

  abstract LocalDate date();

  public static Availability create(String email, TimeRange when, LocalDate date) {
    return builder()
        .setEmail(email)
        .setWhen(when)
        .setDate(date)
        .build();
  }

  static Builder builder() {
    return new AutoValue_Availability.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setEmail(String email);

    abstract Builder setWhen(TimeRange when);

    abstract Builder setDate(LocalDate date);

    abstract Availability build();
  }
}
