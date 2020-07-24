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
import com.google.sps.data.Availability;
import com.google.sps.data.AvailabilityDao;
import com.google.sps.data.DatastoreAvailabilityDao;
import com.google.sps.data.DatastorePersonDao;
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.InterviewPostRequest;
import com.google.sps.data.Person;
import com.google.sps.data.PersonDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.TimeRange;
import java.io.IOException;
import java.io.BufferedReader;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeParseException;

@WebServlet("/scheduled-interviews")
public class ScheduledInterviewServlet extends HttpServlet {

  private ScheduledInterviewDao scheduledInterviewDao;
  private AvailabilityDao availabilityDao;
  private final UserService userService = UserServiceFactory.getUserService();
  private ShowInterviewersServlet interviewerServlet = new ShowInterviewersServlet();

  @Override
  public void init() {
    init(new DatastoreScheduledInterviewDao(), new DatastoreAvailabilityDao(), new DatastorePersonDao());
  }

  public void init(ScheduledInterviewDao scheduledInterviewDao, AvailabilityDao availabilityDao, PersonDao personDao) {
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.availabilityDao = availabilityDao;
    interviewerServlet.init(availabilityDao, personDao);
  }

  // Gets the current user's email from request and returns the ScheduledInterviews for that person.
  // If the email that is requested matches the email that is logged in, then the scheduled
  // interviews are returned, otherwise SC_UNAUTHORIZED is returned.
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userEmail = userService.getCurrentUser().getEmail();
    String userId = userService.getCurrentUser().getUserId();
    if (userId == null) {
      userId = String.format("%d", userEmail.hashCode());
    }
    List<ScheduledInterview> scheduledInterviews = scheduledInterviewDao.getForPerson(userId);
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

    String interviewerCompany = postRequest.getCompany();
    String interviewerJob = postRequest.getJob();
    String utc = postRequest.getUtc();

    Instant startTime;
    Instant endTime;

    try {
      startTime = Instant.parse(utc);
      endTime = Instant.parse(utc).plus(1, ChronoUnit.HOURS);
    } catch (DateTimeParseException e) {
      response.sendError(400, e.getMessage());
      return;
    }

    // TODO: Create Person dao method to get people with this company and job. Then find the ones
    // that are available now and randomly select one.
    
    List<Availability> availabilitiesInRange =
        availabilityDao.getInRangeForAll(startTime, endTime);
    List<Person> allAvailableInterviewers = interviewerServlet.getPossiblePeople(availabilitiesInRange, startTime, endTime);
    List<String> possibleInterviewers = getPossibleInterviewerIds(allAvailableInterviewers, interviewerCompany, interviewerJob);

    // TODO: Randomly select an interviewer (ask how this could be consistent/testable).

    String interviewerId = "fakeId";

    scheduledInterviewDao.create(
        ScheduledInterview.create(
            -1, new TimeRange(startTime, endTime), interviewerId, intervieweeId));

    // Since an interview was scheduled, both parties' availabilities must be updated
    List<Availability> affectedAvailability = new ArrayList<Availability>();
    List<Availability> intervieweeAffectedAvailability =
        availabilityDao.getInRangeForUser(intervieweeId, startTime, endTime);
    List<Availability> interviewerAffectedAvailability =
        availabilityDao.getInRangeForUser(interviewerId, startTime, endTime);
    affectedAvailability.addAll(intervieweeAffectedAvailability);
    affectedAvailability.addAll(interviewerAffectedAvailability);

    for (Availability avail : affectedAvailability) {
      availabilityDao.update(avail.scheduledStatus(true));
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
  
  List<String> getPossibleInterviewerIds(List<Person> availablePeople, String company, String job) {
    List<String> possibleInterviewerIds = new ArrayList<String>();
    for (Person person : availablePeople) {
      if (person.company().equals(company) && person.job().equals(job)) {
        possibleInterviewerIds.add(person.id());
      }
    }
    return possibleInterviewerIds;
  }
}
