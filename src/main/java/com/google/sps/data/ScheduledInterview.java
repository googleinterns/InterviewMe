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
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.ryanharter.auto.value.gson.AutoValueGsonBuilder;

/** Represents a scheduled interview. */
@AutoValue
public abstract class ScheduledInterview {
  @SerializedName("id")
  public abstract long id();

  @SerializedName("when")
  public abstract TimeRange when();

  @SerializedName("interviewerEmail")
  public abstract String interviewerEmail();

  @SerializedName("intervieweeEmail")
  public abstract String intervieweeEmail();

  public static TypeAdapter<ScheduledInterview> typeAdapter(Gson gson) {
    return new AutoValue_ScheduledInterview.GsonTypeAdapter(gson);
  }

  /**
   * Creates a scheduled interview that contains a timerange, the date and the emails of the
   * attendees.
   */
  public static ScheduledInterview create(
      long id, TimeRange when, String interviewerEmail, String intervieweeEmail) {
    return builder()
        .setId(id)
        .setWhen(when)
        .setInterviewerEmail(interviewerEmail)
        .setIntervieweeEmail(intervieweeEmail)
        .build();
  }

  static Builder builder() {
    return new AutoValue_ScheduledInterview.Builder();
  }

  @AutoValueGsonBuilder
  static Builder builderWithDefaults() {
    return builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setId(long id);

    abstract Builder setWhen(TimeRange range);

    abstract Builder setInterviewerEmail(String email);

    abstract Builder setIntervieweeEmail(String email);

    abstract ScheduledInterview build();
  }
}
