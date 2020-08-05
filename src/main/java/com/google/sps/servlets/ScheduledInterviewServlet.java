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

import com.google.api.services.calendar.Calendar;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Availability;
import com.google.sps.data.AvailabilityDao;
import com.google.sps.data.CalendarAccess;
import com.google.sps.data.DatastoreAvailabilityDao;
import com.google.sps.data.DatastorePersonDao;
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.EmailSender;
import com.google.sps.data.GoogleCalendarAccess;
import com.google.sps.data.InterviewPostRequest;
import com.google.sps.data.Job;
import com.google.sps.data.Person;
import com.google.sps.data.PersonDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.ScheduledInterviewRequest;
import com.google.sps.data.SecretFetcher;
import com.google.sps.data.SendgridEmailSender;
import com.google.sps.data.TimeRange;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.BufferedReader;
import java.security.GeneralSecurityException;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
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
  private AvailabilityDao availabilityDao;
  private PersonDao personDao;
  private EmailSender emailSender;
  private CalendarAccess calendarAccess;
  private Calendar service;
  private final UserService userService = UserServiceFactory.getUserService();
  private Path emailsPath =
      Paths.get(
          System.getProperty("user.home") + "/InterviewMe/src/main/resources/templates/email");

  @Override
  public void init() {
    EmailSender emailer;
    try {
      emailer = new SendgridEmailSender(new Email("interviewme.business@gmail.com"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    CalendarAccess calendar;
    try {
      calendar =
          new GoogleCalendarAccess(
              GoogleCalendarAccess.MakeCalendar(new SecretFetcher("interview-me-step-2020")));
    } catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    init(
        new DatastoreScheduledInterviewDao(),
        new DatastoreAvailabilityDao(),
        new DatastorePersonDao(),
        calendar,
        emailer);
  }

  public void init(
      ScheduledInterviewDao scheduledInterviewDao,
      AvailabilityDao availabilityDao,
      PersonDao personDao,
      CalendarAccess calendarAccess,
      EmailSender emailSender) {
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.availabilityDao = availabilityDao;
    this.personDao = personDao;
    this.calendarAccess = calendarAccess;
    this.emailSender = emailSender;
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
    String utcStartTime = postRequest.getUtcStartTime();
    String position = postRequest.getPosition();
    Job selectedPosition = Job.valueOf(Job.class, position);
    TimeRange interviewRange;

    try {
      interviewRange =
          new TimeRange(
              Instant.parse(utcStartTime), Instant.parse(utcStartTime).plus(1, ChronoUnit.HOURS));
    } catch (DateTimeParseException e) {
      response.sendError(400, e.getMessage());
      return;
    }

    List<Person> allAvailableInterviewers =
        ShowInterviewersServlet.getPossiblePeople(
            personDao, availabilityDao, selectedPosition, interviewRange);
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
            Job.valueOf(position),
            /*shadowId=*/ ""));

    HashMap<String, String> emailedDetails = new HashMap<String, String>();
    ScheduledInterview scheduledInterview =
        scheduledInterviewDao
            .getScheduledInterviewsInRangeForUser(
                intervieweeId, interviewRange.start(), interviewRange.end())
            .get(0);
    String interviewId = String.valueOf(scheduledInterview.id());
    String intervieweeFeedbackLink =
        String.format(
            "http://interview-me-step-2020.appspot.com/feedback.html?interview=%s&role=interviewee",
            interviewId);
    String interviewerFeedbackLink =
        String.format(
            "http://interview-me-step-2020.appspot.com/feedback.html?interview=%s&role=interviewer",
            interviewId);
    try {
      meetLink = calendarAccess.getMeetLink(scheduledInterview);
    } catch (GeneralSecurityException e) {
      response.sendError(500);
      return;
    }
    scheduledInterviewDao.update(updatedMeetLinkField(meetLink, scheduledInterview));
    System.out.println(meetLink);
    emailedDetails.put("{{formatted_date}}", getEmailDateString(interviewRange));
    emailedDetails.put("{{interviewer_first_name}}", getFirstName(interviewerId));
    emailedDetails.put("{{interviewee_first_name}}", getFirstName(intervieweeId));
    emailedDetails.put("{{form_link}}", intervieweeFeedbackLink);
    emailedDetails.put("{{position}}", formatPositionString(position));
    emailedDetails.put("{{chat_link}}", meetLink);

    try {
      sendIntervieweeEmail(intervieweeId, emailedDetails);
      emailedDetails.put("{{form_link}}", interviewerFeedbackLink);
      sendInterviewerEmail(interviewerId, emailedDetails);
    } catch (Exception e) {
      response.sendError(500);
      return;
    }

    // Since an interview was scheduled, both parties' availabilities must be updated.
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

  // Gets the formatted date for the string that is shown on the scheduledInterviews page.
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
    String interviewer = getFirstName(scheduledInterview.interviewerId());
    String interviewee = getFirstName(scheduledInterview.intervieweeId());
    String shadow = getFirstName(scheduledInterview.shadowId());
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

  // Gets formatted date for the string that is used in the email sent to users
  private String getEmailDateString(TimeRange when) {
    LocalDateTime start = LocalDateTime.ofInstant(when.start(), ZoneId.systemDefault());
    String startTime = start.format(DateTimeFormatter.ofPattern("h:mm a"));
    String day = start.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
    return String.format("%s at %s UTC", day, startTime);
  }

  private String getEmail(String participantId) {
    return personDao.get(participantId).map(Person::email).orElse("None");
  }

  private String getFirstName(String participantId) {
    return personDao.get(participantId).map(Person::firstName).orElse("None");
  }

  private void sendInterviewerEmail(String interviewerId, HashMap<String, String> emailedDetails)
      throws IOException, Exception {
    String subject = "You have been requested to conduct a mock interview!";
    Email recipient = new Email(getEmail(interviewerId));
    String contentString =
        emailSender.fileContentToString(emailsPath + "/NewInterview_Interviewer.txt");
    Content content =
        new Content("text/plain", emailSender.replaceAllPairs(emailedDetails, contentString));
    emailSender.sendEmail(recipient, subject, content);
  }

  private void sendIntervieweeEmail(String intervieweeId, HashMap<String, String> emailedDetails)
      throws IOException, Exception {
    String subject = "You have been registered for a mock interview!";
    Email recipient = new Email(getEmail(intervieweeId));
    String contentString =
        emailSender.fileContentToString(emailsPath + "/NewInterview_Interviewee.txt");
    Content content =
        new Content("text/plain", emailSender.replaceAllPairs(emailedDetails, contentString));
    emailSender.sendEmail(recipient, subject, content);
  }

  static String formatPositionString(String str) {
    String splitString[] = str.split("_", 0);
    String formattedPositionString = "";
    for (String s : splitString) {
      formattedPositionString += s.substring(0, 1) + s.substring(1).toLowerCase() + " ";
    }
    return formattedPositionString;
  }

  private ScheduledInterview updatedMeetLinkField(
      String meetLink, ScheduledInterview previousInterviewObject) {
    return ScheduledInterview.create(
        previousInterviewObject.id(),
        previousInterviewObject.when(),
        previousInterviewObject.interviewerId(),
        previousInterviewObject.intervieweeId(),
        meetLink,
        previousInterviewObject.position(),
        /*shadowId=*/ "");
  }
}
