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

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.api.services.calendar.Calendar;
import com.google.sps.data.Utils;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.client.util.DateTime;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Google Calendar Data API App Engine sample. */
@WebServlet("/calendar")
public class CalendarServlet extends AbstractAppEngineAuthorizationCodeServlet {

  static final String APP_NAME = "Google Calendar Data API Sample Web Client";

  static final String GWT_MODULE_NAME = "calendar";

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Calendar service = Utils.loadCalendarClient();
    AuthorizationCodeFlow flow = initializeFlow();
    System.out.println("flow inited");
    String url =
        flow.newAuthorizationUrl().setState("xyz").setRedirectUri(getRedirectUri(request)).build();
    System.out.println("url: " + url);
    response.sendRedirect(url);
  }

  @Override
  public String getRedirectUri(HttpServletRequest req) throws IOException {
    return Utils.getRedirectUri(req);
  }

  @Override
  public AuthorizationCodeFlow initializeFlow() throws IOException {
    return Utils.newFlow();
  }

  @Override
  protected String getUserId(HttpServletRequest req) throws IOException {
    return UserServiceFactory.getUserService().getCurrentUser().getUserId();
  }
}
