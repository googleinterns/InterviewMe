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

public enum Job {
  SOFTWARE_ENGINEER("Software Engineer"),
  PRODUCT_MANAGER("Product Manager"),
  BUSINESS_ANALYST("Business Analyst"),
  TECHNICAL_CONSULTANT("Technical Consultant"),
  SOFTWARE_TESTER("Software Tester"),
  TECHNICAL_SALES("Technical Sales"),
  NETWORK_ENGINEER("Network Engineer"),
  SYSTEMS_ANALYST("Systems Analyst"),
  TECHNICAL_SUPPORT("Technical Support");

  String formattedJob;

  Job(String formattedJob) {
    this.formattedJob = formattedJob;
  }

  @Override
  public String toString() {
    return formattedJob;
  }
}
