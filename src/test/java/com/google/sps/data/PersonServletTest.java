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

package com.google.sps.data;

import com.google.gson.Gson;
import com.google.sps.servlets.PersonServlet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** */
@RunWith(JUnit4.class)
public final class PersonServletTest {
  LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
  //
  @Test
  public void getOneOutOfTwo() throws IOException, UnsupportedEncodingException {
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    postRequest.addParameter("user-email", "a@gmail.com");
    postRequest.addParameter("first-name", "a");
    postRequest.addParameter("last-name", "a");
    postRequest.addParameter("company", "");
    postRequest.addParameter("job", "");
    postRequest.addParameter("linkedin", "");

    // Post person a.
    PersonServlet personServlet = new PersonServlet();
    personServlet.init(new FakePersonDao());
    personServlet.doPost(postRequest, new MockHttpServletResponse());

    // Post person b.
    postRequest.setParameter("user-email", "b@gmail.com");
    postRequest.addParameter("first-name", "b");
    postRequest.addParameter("last-name", "b");
    personServlet.doPost(postRequest, new MockHttpServletResponse());

    // b is logged in.
    helper.setEnvIsLoggedIn(true).setEnvEmail("b@gmail.com").setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("email", "b@gmail.com");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personServlet.doGet(getRequest, getResponse);

    String json = getResponse.getContentAsString();
    System.out.println(json);
    Gson g = new Gson();
    Person p = g.fromJson(json, Person.class);
    System.out.println(p.firstName());
  }
}
