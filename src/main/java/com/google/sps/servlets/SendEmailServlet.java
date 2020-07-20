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

import com.google.sps.data.EmailUtility;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/send-email")
public class SendEmailServlet extends HttpServlet {
  private String host;
  private String port;
  private String user;
  private String pass;

  public void init() {
    host = "smtp.gmail.com";
    port = "587";
    user = "interviewme.business@gmail.com";
    pass = "#InterviewM3!";
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // reads form fields
    String subject = "Test subject";
    String recipient = "the.claire.yang@gmail.com";
    String content = "Did you get this?";

    String resultMessage = "";

    try {
      EmailUtility.sendEmail(host, port, user, pass, recipient, subject, content);
      resultMessage = "The e-mail was sent successfully";
    } catch (Exception ex) {
      ex.printStackTrace();
      resultMessage = "There were an error: " + ex.getMessage();
    } finally {
      response
          .getWriter()
          .print(
              "Send email to "
                  + recipient
                  + "<br/>with subject : "
                  + subject
                  + "<br/>Message : "
                  + content);
      response.getWriter().print("<br/>Status : " + resultMessage);
    }
  }
}
