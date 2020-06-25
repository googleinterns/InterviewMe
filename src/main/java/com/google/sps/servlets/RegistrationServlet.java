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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

@WebServlet("/register")
public class RegistrationServlet extends HttpServlet {
  private DatastoreService datastore;

  @Override
  public void init() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Sends info from the registration form to Datastore in the form of a new user.
    userEntity.setProperty("email", request.getParameter("user-email"));
    userEntity.setProperty("firstName", request.getParameter("first-name"));
    userEntity.setProperty("lastName", request.getParameter("last-name"));
    userEntity.setProperty("company", request.getParameter("company"));
    userEntity.setProperty("job", request.getParameter("job"));
    userEntity.setProperty("linkedin", request.getParameter("linkedin"));
    datastore.put(userEntity);
    response.sendRedirect("/");
  }
}
