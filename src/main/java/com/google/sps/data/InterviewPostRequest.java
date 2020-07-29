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

import java.util.ArrayList;

/**
 * An InterviewPostRequest is used to schedule an interview for the current user with an interviewer
 * who has the selected company and job and is available at the specified time, which is represented
 * by the utc String.
 */
public class InterviewPostRequest {
  private String company;
  private String job;
  private String utc;

  public InterviewPostRequest(String company, String job, String utc) {
    this.company = company;
    this.job = job;
    this.utc = utc;
  }

  public String getCompany() {
    return company;
  }

  public String getJob() {
    return job;
  }

  public String getUtc() {
    return utc;
  }

  public boolean allFieldsPopulated() {
    return !(company == null || job == null || utc == null);
  }

  public String toString() {
    return String.format("InterviewPostRequest= company:%s, job:%s, utc:%s", company, job, utc);
  }
}
