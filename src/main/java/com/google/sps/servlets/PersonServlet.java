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
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.sps.data.DatastorePersonDao;
import com.google.sps.data.Person;
import com.google.sps.data.PersonDao;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@WebServlet("/person")
public class PersonServlet extends HttpServlet {

  private PersonDao personDao;

  @Override
  public void init() {
    init(new DatastorePersonDao());
  }

  // TODO: add a FakePersonDao class so this will become useful
  public void init(PersonDao personDao) {
    this.personDao = personDao;
  }

  // Sends the request's contents to Datastore in the form of a new Person.
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    personDao.create(
        Person.create(
            request.getParameter("user-email"),
            request.getParameter("first-name"),
            request.getParameter("last-name"),
            request.getParameter("company"),
            request.getParameter("job"),
            request.getParameter("linkedin")));
  }

  // Updates Datastore with the Person information in request.
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    personDao.update(
        Person.create(
            request.getParameter("user-email"),
            request.getParameter("first-name"),
            request.getParameter("last-name"),
            request.getParameter("company"),
            request.getParameter("job"),
            request.getParameter("linkedin")));
    // TODO: redirect to home page and handle in JS.
  }

  // Returns the person the request's email belongs to. If they aren't in Datastore, redirects to
  // registration page. If the requestee is not the logged in user, throws a
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Ensure person logged in == person being requested.
    String requesteeEmail = request.getParameter("email"),
        userEmail = LogInServlet.getLoginInfo("/").email;
    Preconditions.checkState(requesteeEmail.equals(userEmail));

    Optional<Person> personOpt = personDao.get(requesteeEmail);
    if (!personOpt.isPresent()) {
      // TODO: apply this to all other pages when someone accesses them illegally.
      response.sendRedirect("/register.html");
      return;
    }

    response.setContentType("application/json;");
    response.getWriter().println(new Gson().toJson(personOpt.get()));
  }
}
