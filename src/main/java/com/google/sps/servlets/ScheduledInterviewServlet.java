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
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.TimeRange;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeParseException;

@WebServlet("/scheduled-interviews")
public class ScheduledInterviewServlet extends HttpServlet {

  private ScheduledInterviewDao scheduledInterviewDao;
  private final UserService userService = UserServiceFactory.getUserService();

  @Override
  public void init() {
    init(new DatastoreScheduledInterviewDao());
  }

  // TODO: add a FakeScheduledInterviewDao class so this will become useful
  public void init(ScheduledInterviewDao scheduledInterviewDao) {
    this.scheduledInterviewDao = scheduledInterviewDao;
  }

  // Gets the current user's email from request and returns the ScheduledInterviews for that person.
  // If the email that is requested matches the email that is logged in, then the scheduled
  // interviews are returned, otherwise SC_UNAUTHORIZED is returned.
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userEmail = userService.getCurrentUser().getEmail();
    List<ScheduledInterview> scheduledInterviews = scheduledInterviewDao.getForPerson(userEmail);
    response.setContentType("application/json;");
    response.getWriter().println(new Gson().toJson(scheduledInterviews));
  }

  // Send the request's contents to Datastore in the form of a new ScheduledInterview object.
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    String intervieweeEmail = userService.getCurrentUser().getEmail();
    String intervieweeId = userService.getCurrentUser().getUserId();
    
    // Since UserId does not have a valid Mock, if the id is null (as when testing), it will be
    // replaced with this hashcode.
    if (intervieweeId == null) {
      intervieweeId = String.format("%d", intervieweeEmail.hashCode());
    }
    
    
    InterviewPostRequest postRequest;
    try {
      postRequest = new Gson().fromJson(getJsonString(request), InterviewPostRequest.class);
    } catch (Exception JsonSyntaxException) {
      response.sendError(400);
      return;
    }
    if (!postRequest.allFieldsPopulated()) {
      response.sendError(400);
      return;
    }
    
    String interviewerId = postRequest.getInterviewer();
    String utc = postRequest.getUtc();
    
    String requestedInterviewerEmail = request.getParameter("interviewer");
    String requestedIntervieweeEmail = request.getParameter("interviewee");
    String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
    // The default key for a scheduledInterview being stored in datastore
    long defaultKey = -1;
    try {
      ScheduledInterview scheduledInterview =
          ScheduledInterview.create(
              defaultKey,
              new TimeRange(
                  Instant.parse(request.getParameter("startTime")),
                  Instant.parse(request.getParameter("endTime"))),
              requestedInterviewerEmail,
              requestedIntervieweeEmail);
      scheduledInterviewDao.create(scheduledInterview);
      return;
    } catch (DateTimeParseException e) {
      response.sendError(400, e.getMessage());
      return;
    }
  }
  
  // Get Json from request body.
  private static String getJsonString(HttpServletRequest request) throws IOException {
    BufferedReader reader = request.getReader();
    StringBuffer buffer = new StringBuffer();
    String payloadLine = null;

    while ((payloadLine = reader.readLine()) != null) buffer.append(payloadLine);
    return buffer.toString();
  }
}
