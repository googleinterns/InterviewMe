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

import com.google.appengine.tools.development.testing.LocalCapabilitiesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.sps.servlets.ScheduledInterviewServlet;
import com.google.sps.data.PutAvailabilityRequest;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.Test;
import com.google.gson.JsonSyntaxException;

@RunWith(JUnit4.class)
public final class ScheduledInterviewServletTest {
  LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalCapabilitiesServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
  // Tests whether a scheduledInterview object was added to datastore.
  @Test
  public void validScheduledInterviewServletPostRequest() throws IOException {
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(new FakeScheduledInterviewDao());
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    postRequest.addParameter("startTime", "2020-07-05T18:30:10Z");
    postRequest.addParameter("endTime", "2020-07-05T19:30:10Z");
    postRequest.addParameter("interviewer", "user@company.org");
    postRequest.addParameter("interviewee", "user@gmail.com");

    scheduledInterviewServlet.doPost(postRequest, postResponse);
    Assert.assertEquals(200, postResponse.getStatus());
  }

  // Tests whether a list of scheduledInterviews was returned by the server
  @Test
  public void validScheduledInterviewServletGetRequest() throws IOException {
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(new FakeScheduledInterviewDao());
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    MockHttpServletRequest postRequest = new MockHttpServletRequest();

    postRequest.addParameter("startTime", "2020-07-05T18:30:10Z");
    postRequest.addParameter("endTime", "2020-07-05T19:30:10Z");
    postRequest.addParameter("interviewer", "user@company.org");
    postRequest.addParameter("interviewee", "user@gmail.com");
    scheduledInterviewServlet.doPost(postRequest, new MockHttpServletResponse());

    postRequest.setParameter("startTime", "2020-07-05T20:30:10Z");
    postRequest.setParameter("endTime", "2020-07-05T21:30:10Z");
    postRequest.setParameter("interviewer", "user2@company.org");
    postRequest.setParameter("interviewee", "user@gmail.com");
    scheduledInterviewServlet.doPost(postRequest, new MockHttpServletResponse());

    postRequest.setParameter("startTime", "2020-07-05T18:30:10Z");
    postRequest.setParameter("endTime", "2020-07-05T19:30:10Z");
    postRequest.setParameter("interviewer", "user2@company.org");
    postRequest.setParameter("interviewee", "user1@gmail.com");
    scheduledInterviewServlet.doPost(postRequest, new MockHttpServletResponse());

    getRequest.addParameter("userEmail", "user@gmail.com");
    scheduledInterviewServlet.doGet(getRequest, getResponse);

    Assert.assertEquals(200, getResponse.getStatus());
  }
}
