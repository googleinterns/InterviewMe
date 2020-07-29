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

// Servlet that gets the feedback for an interviewer
@WebServlet("/interviewer-feedback")
public class InterviewerFeedbackServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String question1 = request.getParameter("question1"); 
    String question2 = request.getParameter("question2"); 
    String question3 = request.getParameter("question3"); 
    String question4 = request.getParameter("question4"); 
    String question5 = request.getParameter("question5"); 
    String question6 = request.getParameter("question6"); 
    String question7 = request.getParameter("question7"); 
    String question8 = request.getParameter("question8"); 
    String question9 = request.getParameter("question9"); 
  }
}