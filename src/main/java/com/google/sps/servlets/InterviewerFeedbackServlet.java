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
import com.google.sps.data.DatastorePersonDao;
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.EmailSender;
import com.google.sps.data.Person;
import com.google.sps.data.PersonDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

// Servlet that gets the feedback from an interviewee and sends it to an interviewer.
@WebServlet("/interviewer-feedback")
public class InterviewerFeedbackServlet extends HttpServlet {
  private ScheduledInterviewDao scheduledInterviewDao;
  private PersonDao personDao;
  private Path emailsPath =
      Paths.get(
          System.getProperty("user.home") + "/InterviewMe/src/main/resources/templates/email");

  @Override
  public void init() {
    init(new DatastoreScheduledInterviewDao(), new DatastorePersonDao());
  }

  public void init(ScheduledInterviewDao scheduledInterviewDao, PersonDao personDao) {
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.personDao = personDao;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long scheduledInterviewId = Long.parseLong(request.getParameter("interviewId"));
    int numberOfQuestions = Integer.parseInt(request.getParameter("questionCount"));
    HashMap<String, String> answers = new HashMap<String, String>();
    for (int i = 1; i <= numberOfQuestions; i++) {
      String template = String.format("{{question_%s}}", i);
      String param = String.format("question%s", i);
      answers.put(template, request.getParameter(param));
    }

    String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    // Since Users returned from the LocalUserService (in tests) do not have userIds, here we set
    // the userId equal to a hashcode.
    if (userId == null) {
      userId = String.format("%d", userEmail.hashCode());
    }

    Optional<ScheduledInterview> scheduledInterviewOpt =
        scheduledInterviewDao.get(scheduledInterviewId);
    if (!scheduledInterviewOpt.isPresent()) {
      System.out.println("Here Too");
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    ScheduledInterview scheduledInterview = scheduledInterviewOpt.get();
    Optional<Person> interviewerOpt = getInterviewer(scheduledInterview);
    answers.put("{{formatted_date}}", scheduledInterview.getDateString());

    if (!isInterviewee(scheduledInterview, userId)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    if (!interviewerOpt.isPresent()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    Person interviewer = interviewerOpt.get();
    try {
      sendFeedback(interviewer.email(), answers);
    } catch (Exception e) {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    response.sendRedirect("/scheduled-interviews.html");
    return;
  }

  private boolean isInterviewee(ScheduledInterview scheduledInterview, String userId) {
    return scheduledInterview.intervieweeId().equals(userId);
  }

  private Optional<Person> getInterviewer(ScheduledInterview scheduledInterview) {
    return personDao.get(scheduledInterview.interviewerId());
  }

  private void sendFeedback(String interviewerEmail, HashMap<String, String> answers)
      throws IOException, Exception {
    EmailSender emailSender = new EmailSender(new Email("interviewme.business@gmail.com"));
    String subject = "Your Interviewee has submitted feedback for your interview!";
    Email recipient = new Email(interviewerEmail);
    String contentString =
        emailSender.fileContentToString(emailsPath + "/feedbackToInterviewer.txt");
    Content content =
        new Content("text/plain", emailSender.replaceAllPairs(answers, contentString));
    emailSender.sendEmail(recipient, subject, content);
  }
}
