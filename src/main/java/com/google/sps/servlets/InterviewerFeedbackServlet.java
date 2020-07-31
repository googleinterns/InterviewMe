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
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

// Servlet that gets the feedback from an interviewee to an interviewer
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
    List<String> answers = new ArrayList<String>();
    answers.add(request.getParameter("question1"));
    answers.add(request.getParameter("question2"));
    answers.add(request.getParameter("question3"));
    answers.add(request.getParameter("question4"));
    answers.add(request.getParameter("question5"));
    answers.add(request.getParameter("question6"));
    answers.add(request.getParameter("question7"));
    answers.add(request.getParameter("question8"));
    answers.add(request.getParameter("question9"));

    String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    // Since UserId does not have a valid Mock, if the id is null (as when testing), it will be
    // replaced with this hashcode.
    if (userId == null) {
      userId = String.format("%d", userEmail.hashCode());
    }

    if (interviewExists(scheduledInterviewId)) {
      if (isInterviewee(scheduledInterviewId, userId)) {
        try {
          sendFeedback("grantflash@gmail.com");
        } catch (Exception e) {
          e.printStackTrace();
          response.sendError(500);

          return;
        }
        response.sendRedirect("/scheduled-interviews.html");
        return;
      } else {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
  }

  private boolean interviewExists(long scheduledInterviewId) {
    return scheduledInterviewDao.get(scheduledInterviewId).isPresent();
  }

  private boolean isInterviewee(long scheduledInterviewId, String userId) {
    ScheduledInterview scheduledInterview = scheduledInterviewDao.get(scheduledInterviewId).get();
    return scheduledInterview.intervieweeId().equals(userId);
  }

  private String getInterviewerEmail(long scheduledInterviewId) {
    ScheduledInterview scheduledInterview = scheduledInterviewDao.get(scheduledInterviewId).get();
    return personDao
        .get(scheduledInterview.interviewerId())
        .map(Person::email)
        .orElse("Email not found");
  }

  private void sendFeedback(String intervieweeEmail) throws IOException, Exception {
    EmailSender emailSender = new EmailSender(new Email("interviewme.business@gmail.com"));
    String subject = "Your Interviewee has submitted some feedback for your interview!";
    Email recipient = new Email(intervieweeEmail);
    String contentString =
        EmailSender.fileContentToString(emailsPath + "/feedbackToInterviewer.txt");
    Content content = new Content("text/plain", contentString);
    emailSender.sendEmail(recipient, subject, content);
  }
}
