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
import java.util.ArrayList;

@AutoValue
public abstract class AvailabilityJSONConverter {
  public abstract String firstSlotUTC();

  public abstract String lastSlotUTC();

  public abstract ArrayList<String> selectedSlotsUTC();

  static AvailabilityJSONConverter create(
      String firstSlotUTC, String lastSlotUTC, ArrayList<String> selectedSlotsUTC) {
    return new AutoValue_AvailabilityJSONConverter(firstSlotUTC, lastSlotUTC, selectedSlotsUTC);
  }
}
