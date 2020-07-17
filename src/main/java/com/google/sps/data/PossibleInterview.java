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
import com.google.common.annotations.VisibleForTesting;

// FIX DESCRIPTION

/**
 * A PossibleInterview represents a 1 hour chunk of time that has a interviewer and a 
 * time range.
 */
@AutoValue
public abstract class PossibleInterview {
  public abstract String interviewer();

  public abstract TimeRange when();

  @VisibleForTesting
  static PossibleInterview create(
      String interviewer, TimeRange when) {
    return builder()
        .setInterviewer(interviewer)
        .setWhen(when)
        .build();
  }

  static Builder builder() {
    return new AutoValue_PossibleInterview.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setInterviewer(String interviewer);

    abstract Builder setWhen(TimeRange when);

    abstract PossibleInterview build();
  }
}
