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
import com.google.gson.JsonSyntaxException;
import com.google.sps.data.Availability;
import com.google.sps.data.AvailabilityDao;
import com.google.sps.data.DatastoreAvailabilityDao;
import com.google.sps.data.DatastorePersonDao;
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.InterviewPostOrPutRequest;
import com.google.sps.data.Job;
import com.google.sps.data.Person;
import com.google.sps.data.PersonDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.ScheduledInterviewRequest;
import com.google.sps.data.TimeRange;
import java.io.IOException;
import java.io.BufferedReader;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
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
  private AvailabilityDao availabilityDao;
  private PersonDao personDao;
  private final UserService userService = UserServiceFactory.getUserService();

  @Override
  public void init() {
    init(
        new DatastoreScheduledInterviewDao(),
        new DatastoreAvailabilityDao(),
        new DatastorePersonDao());
  }

  public void init(
      ScheduledInterviewDao scheduledInterviewDao,
      AvailabilityDao availabilityDao,
      PersonDao personDao) {
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.availabilityDao = availabilityDao;
    this.personDao = personDao;
  }

  // Gets the current user's email and returns the ScheduledInterviews for that person.
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String timeZoneId = request.getParameter("timeZone");
    String userTime = request.getParameter("userTime");
    String userEmail = userService.getCurrentUser().getEmail();
    String userId = getUserId();
    List<ScheduledInterviewRequest> scheduledInterviews =
        scheduledInterviewsToRequestObjects(
            scheduledInterviewDao.getForPerson(userId), timeZoneId, userTime);
    request.setAttribute("scheduledInterviews", scheduledInterviews);
    RequestDispatcher rd = request.getRequestDispatcher("/scheduled-interviews.jsp");
    try {
      rd.forward(request, response);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }

  // Send the request's contents to Datastore in the form of a new ScheduledInterview object.
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String intervieweeEmail = userService.getCurrentUser().getEmail();
    String intervieweeId = getUserId();
    InterviewPostOrPutRequest postRequest;
    try {
      postRequest = new Gson().fromJson(getJsonString(request), InterviewPostOrPutRequest.class);
    } catch (JsonSyntaxException jse) {
      response.sendError(400);
      return;
    }
    if (!postRequest.allFieldsPopulated()) {
      response.sendError(400);
      return;
    }
    String utcStartTime = postRequest.getUtcStartTime();
    TimeRange interviewRange;
    try {
      interviewRange =
          new TimeRange(
              Instant.parse(utcStartTime), Instant.parse(utcStartTime).plus(1, ChronoUnit.HOURS));
    } catch (DateTimeParseException e) {
      response.sendError(400, e.getMessage());
      return;
    }
    String position = postRequest.getPosition();
    Job selectedPosition = Job.valueOf(Job.class, position);
    List<Person> allAvailableInterviewers =
        ShowInterviewersServlet.getPossiblePeople(
            personDao, availabilityDao, selectedPosition, interviewRange);
    String interviewerCompany = postRequest.getCompany();
    String interviewerJob = postRequest.getJob();
    List<String> possibleInterviewers =
        getPossibleInterviewerIds(allAvailableInterviewers, interviewerCompany, interviewerJob);
    int randomNumber = (int) (Math.random() * possibleInterviewers.size());
    String interviewerId = possibleInterviewers.get(randomNumber);
    // TODO: replace with real MeetLink from Calendar Access
    String meetLink = "meet_link";
    // Shadow is empty because when an interview is first made, only interviewee and
    // interviewer are involved.
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            -1,
            interviewRange,
            interviewerId,
            intervieweeId,
            meetLink,
            selectedPosition,
            /*shadowId=*/ ""));
    // Since an interview was scheduled, both parties' availabilities must be updated
    List<Availability> affectedAvailability = new ArrayList<Availability>();
    List<Availability> intervieweeAffectedAvailability =
        availabilityDao.getInRangeForUser(
            intervieweeId, interviewRange.start(), interviewRange.end());
    List<Availability> interviewerAffectedAvailability =
        availabilityDao.getInRangeForUser(
            interviewerId, interviewRange.start(), interviewRange.end());
    affectedAvailability.addAll(intervieweeAffectedAvailability);
    affectedAvailability.addAll(interviewerAffectedAvailability);
    for (Availability avail : affectedAvailability) {
      availabilityDao.update(avail.withScheduled(true));
    }
  }

  // Send the request's contents to Datastore in the form of an updated ScheduledInterview object.
  // Adds the current user as a shadow.
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String shadowEmail = userService.getCurrentUser().getEmail();
    String shadowId = getUserId();
    InterviewPostOrPutRequest putRequest;
    try {
      putRequest = new Gson().fromJson(getJsonString(request), InterviewPostOrPutRequest.class);
    } catch (JsonSyntaxException jse) {
      response.sendError(400);
      return;
    }
    if (!putRequest.allFieldsPopulated()) {
      response.sendError(400);
      return;
    }
    String utcStartTime = putRequest.getUtcStartTime();
    TimeRange interviewRange;
    try {
      interviewRange =
          new TimeRange(
              Instant.parse(utcStartTime), Instant.parse(utcStartTime).plus(1, ChronoUnit.HOURS));
    } catch (DateTimeParseException e) {
      response.sendError(400, e.getMessage());
      return;
    }
    String position = putRequest.getPosition();
    Job selectedPosition = Job.valueOf(Job.class, position);
    List<ScheduledInterview> possibleInterviews =
        ShadowLoadInterviewsServlet.getPossibleInterviews(
            scheduledInterviewDao, selectedPosition, interviewRange, personDao, shadowId);
    Set<ScheduledInterview> notValidInterviews = new HashSet<ScheduledInterview>();
    // We want to remove all interviews where the company or job does not match that
    // specified in the request.
    String interviewerCompany = putRequest.getCompany();
    String interviewerJob = putRequest.getJob();
    for (ScheduledInterview interview : possibleInterviews) {
      if (!personDao.get(interview.interviewerId()).get().company().equals(interviewerCompany)
          || !personDao.get(interview.interviewerId()).get().job().equals(interviewerJob)) {
        notValidInterviews.add(interview);
      }
    }
    possibleInterviews.removeAll(notValidInterviews);
    int randomNumber = (int) (Math.random() * possibleInterviews.size());
    ScheduledInterview selectedInterview = possibleInterviews.get(randomNumber);
    scheduledInterviewDao.update(selectedInterview.withShadow(shadowId));
    // Since the shadow commited to this interview, their availabilities must be updated
    List<Availability> affectedAvailability =
        availabilityDao.getInRangeForUser(shadowId, interviewRange.start(), interviewRange.end());
    for (Availability avail : affectedAvailability) {
      availabilityDao.update(avail.withScheduled(true));
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

  public List<ScheduledInterviewRequest> scheduledInterviewsToRequestObjects(
      List<ScheduledInterview> scheduledInterviews,
      String timeZoneIdString,
      String userTimeString) {
    ZoneId timeZoneId = ZoneId.of(timeZoneIdString);
    Instant userTime = Instant.parse(userTimeString);
    List<ScheduledInterviewRequest> requestObjects = new ArrayList<ScheduledInterviewRequest>();
    for (ScheduledInterview scheduledInterview : scheduledInterviews) {
      requestObjects.add(makeScheduledInterviewRequest(scheduledInterview, timeZoneId, userTime));
    }
    return requestObjects;
  }

  private String getDateString(TimeRange when, ZoneId timeZoneId) {
    LocalDateTime start = LocalDateTime.ofInstant(when.start(), timeZoneId);
    LocalDateTime end = LocalDateTime.ofInstant(when.end(), timeZoneId);
    String startTime = start.format(DateTimeFormatter.ofPattern("h:mm a"));
    String endTime = end.format(DateTimeFormatter.ofPattern("h:mm a"));
    String day = start.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
    return String.format("%s from %s to %s", day, startTime, endTime);
  }

  private ScheduledInterviewRequest makeScheduledInterviewRequest(
      ScheduledInterview scheduledInterview, ZoneId timeZoneId, Instant userTime) {
    String userEmail = userService.getCurrentUser().getEmail();
    String userId = getUserId();
    String date = getDateString(scheduledInterview.when(), timeZoneId);
    String interviewer =
        personDao
            .get(scheduledInterview.interviewerId())
            .map(Person::firstName)
            .orElse("Nonexistent User");
    String interviewee =
        personDao
            .get(scheduledInterview.intervieweeId())
            .map(Person::firstName)
            .orElse("Nonexistent User");
    // When an interview is first scheduled, the shadowId is set to an empty string. Since this
    // behaviour is expected, here we prevent a null or empty name exception with creating keys
    // in datastore.
    String shadow = "None";
    if (!scheduledInterview.shadowId().equals("")) {
      Optional<String> firstName =
          personDao.get(scheduledInterview.shadowId()).map(Person::firstName);
      if (firstName.isPresent()) {
        shadow = firstName.get();
      }
    }
    String role = getUserRole(scheduledInterview, userId);
    boolean hasStarted =
        scheduledInterview.when().start().minus(5, ChronoUnit.MINUTES).isBefore(userTime);
    String meetLink = scheduledInterview.meetLink();
    String position = scheduledInterview.position().name();
    return new ScheduledInterviewRequest(
        scheduledInterview.id(),
        date,
        interviewer,
        interviewee,
        role,
        hasStarted,
        meetLink,
        position,
        shadow);
  }

  static String getUserRole(ScheduledInterview scheduledInterview, String userId) {
    if (userId.equals(scheduledInterview.interviewerId())) {
      return "Interviewer";
    }
    if (userId.equals(scheduledInterview.intervieweeId())) {
      return "Interviewee";
    }
    if (userId.equals(scheduledInterview.shadowId())) {
      return "Shadow";
    }
    return "unknown";
  }

  private String getUserId() {
    String userId = userService.getCurrentUser().getUserId();
    // Since Users returned from the LocalUserService (in tests) do not have userIds, here we set
    // the userId equal to a hashcode.
    if (userId == null) {
      userId = String.format("%d", userService.getCurrentUser().getEmail().hashCode());
    }
    return userId;
  }
}
