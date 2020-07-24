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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.DatastorePersonDao;
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.Person;
import com.google.sps.data.PersonDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.ScheduledInterviewRequest;
import com.google.sps.data.TimeRange;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.time.format.DateTimeParseException;

@WebServlet("/scheduled-interviews")
public class ScheduledInterviewServlet extends HttpServlet {

  private ScheduledInterviewDao scheduledInterviewDao;
  private PersonDao personDao;

  @Override
  public void init() {
    init(new DatastoreScheduledInterviewDao(), new DatastorePersonDao());
  }

  public void init(ScheduledInterviewDao scheduledInterviewDao, PersonDao personDao) {
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.personDao = personDao;
  }

  // Gets the current user's email from request and returns the ScheduledInterviews for that person.
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String timeZoneId = request.getParameter("timeZone");
    String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    if (userId == null) {
      userId = String.format("%d", userEmail.hashCode());
    }
    List<ScheduledInterviewRequest> scheduledInterviews =
        objectToRequestObject(scheduledInterviewDao.getForPerson(userId), timeZoneId);
    request.setAttribute("scheduledInterviews", scheduledInterviews);
    List<ScheduledInterviewRequest> scheduledInterviews1 =
        (List<ScheduledInterviewRequest>) request.getAttribute("scheduledInterviews");
    RequestDispatcher rd = request.getRequestDispatcher("/scheduled-interviews.jsp");
    try {
      rd.forward(request, response);
    } catch (ServletException e) {
      e.printStackTrace();
    }
  }

  // Send the request's contents to Datastore in the form of a new ScheduledInterview object.
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String requestedInterviewerId = request.getParameter("interviewer");
    String requestedIntervieweeId = request.getParameter("interviewee");
    String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    if (userId == null) {
      userId = String.format("%d", userEmail.hashCode());
    }
    // The default key for a scheduledInterview being stored in datastore
    long defaultKey = -1;
    if ((!requestedInterviewerId.equals(userId)) && (!requestedIntervieweeId.equals(userId))) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    try {
      ScheduledInterview scheduledInterview =
          ScheduledInterview.create(
              defaultKey,
              new TimeRange(
                  Instant.parse(request.getParameter("startTime")),
                  Instant.parse(request.getParameter("endTime"))),
              requestedInterviewerId,
              requestedIntervieweeId);
      scheduledInterviewDao.create(scheduledInterview);
      return;
    } catch (DateTimeParseException e) {
      response.sendError(400, e.getMessage());
      return;
    }
  }

  public List<ScheduledInterviewRequest> objectToRequestObject(
      List<ScheduledInterview> scheduledInterviews, String timeZoneIdString) {
    ZoneId timeZoneId;
    if (timeZoneIdString == null) {
      timeZoneId = ZoneId.systemDefault();
    } else {
      timeZoneId = ZoneId.of(timeZoneIdString);
    }
    List<ScheduledInterviewRequest> requestObjects = new ArrayList<ScheduledInterviewRequest>();
    for (ScheduledInterview scheduledInterview : scheduledInterviews) {
      requestObjects.add(makeSheduledInterviewRequest(scheduledInterview, timeZoneId));
    }
    return requestObjects;
  }

  private String getDateString(TimeRange when, ZoneId timeZoneId) {
    LocalDateTime start = LocalDateTime.ofInstant(when.start(), timeZoneId);
    LocalDateTime end = LocalDateTime.ofInstant(when.end(), timeZoneId);
    String startTime = start.format(DateTimeFormatter.ofPattern("h:mm a"));
    String endTime = end.format(DateTimeFormatter.ofPattern("h:mm a"));
    String day = start.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
    return day + " from " + startTime + " to " + endTime;
  }

  private ScheduledInterviewRequest makeSheduledInterviewRequest(
      ScheduledInterview scheduledInterview, ZoneId timeZoneId) {
    String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    if (userId == null) {
      userId = String.format("%d", userEmail.hashCode());
    }
    String date = getDateString(scheduledInterview.when(), timeZoneId);
    String interviewer;
    String interviewee;
    String role;

    if (userId.equals(scheduledInterview.interviewerId())) {
      role = "Interviewer";
    } else {
      role = "Interviewee";
    }

    if (!personDao.get(scheduledInterview.interviewerId()).isPresent()) {
      interviewer = "Nonexistent User";
    } else {
      interviewer = personDao.get(scheduledInterview.interviewerId()).get().firstName();
    }

    if (!personDao.get(scheduledInterview.intervieweeId()).isPresent()) {
      interviewee = "Nonexistent User";
    } else {
      interviewee = personDao.get(scheduledInterview.intervieweeId()).get().firstName();
    }

    return new ScheduledInterviewRequest(
        scheduledInterview.id(), date, interviewer, interviewee, role);
  }
}
