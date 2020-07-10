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

package com.google.sps.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.TimeRange;

@WebServlet("/new-scheduled-interview")
public class NewScheduledInterviewServlet extends HttpServlet {

  private ScheduledInterviewDao scheduledInterviewDao;

  @Override
  public void init() {
    init(new DatastoreScheduledInterviewDao());
  }

  // TODO: add a FakeScheduledInterviewDao class so this will become useful
  public void init(ScheduledInterviewDao scheduledInterviewDao) {
    this.scheduledInterviewDao = scheduledInterviewDao;
  }

  // Sends the request's contents to Datastore in the form of a new ScheduledInterview.
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
    ScheduledInterview scheduledInterview =
        ScheduledInterview.create(
            0,
            new TimeRange(
                Instant.parse(request.getParameter("startTime")),
                Instant.parse(request.getParameter("endTime"))),
            LocalDate.parse(request.getParameter("date")),
            request.getParameter("interviewer"),
            request.getParameter("interviewee"));
    scheduledInterviewDao.create(scheduledInterview);
  }
}
