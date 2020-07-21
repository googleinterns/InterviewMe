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

import com.google.appengine.tools.development.testing.LocalCapabilitiesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.Availability;
import com.google.sps.data.FakeAvailabilityDao;
import com.google.sps.data.TimeRange;
import java.io.IOException;
import javax.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.Test;
import com.google.gson.JsonSyntaxException;

@RunWith(JUnit4.class)
public final class SearchInterviewServletTest {
  LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalCapabilitiesServiceTestConfig());
  private FakeAvailabilityDao availabilityDao;
  private MockServletContext context;

  @Before
  public void setUp() {
    helper.setUp();
    availabilityDao = new FakeAvailabilityDao();
    context = new MockServletContext();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void tooLargePositiveOffset() {
    SearchInterviewServlet servlet = new SearchInterviewServlet();
    servlet.init(availabilityDao, Instant.now());
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "740");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          servlet.doGet(getRequest, getResponse);
        });
  }

  @Test
  public void tooLargeNegativeOffset() {
    SearchInterviewServlet servlet = new SearchInterviewServlet();
    servlet.init(availabilityDao, Instant.now());
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "-740");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          servlet.doGet(getRequest, getResponse);
        });
  }

  @Test
  public void onlyReturnsHourLongSlots() throws IOException, ServletException {
    SearchInterviewServlet servlet = new SearchInterviewServlet();
    servlet.init(availabilityDao, Instant.parse("2020-07-07T13:15:00Z"));

    // A 15 minute slot
    availabilityDao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-07T13:30:00Z"), Instant.parse("2020-07-07T13:45:00Z")),
            -1,
            false));

    // A 30 minute slot
    availabilityDao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-07T14:30:00Z"), Instant.parse("2020-07-07T14:45:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-07T14:45:00Z"), Instant.parse("2020-07-07T15:00:00Z")),
            -1,
            false));

    // A 45 minute slot
    availabilityDao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-07T15:30:00Z"), Instant.parse("2020-07-07T15:45:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-07T15:45:00Z"), Instant.parse("2020-07-07T16:00:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-07T16:00:00Z"), Instant.parse("2020-07-07T16:15:00Z")),
            -1,
            false));

    // An hour slot
    availabilityDao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-07T16:30:00Z"), Instant.parse("2020-07-07T16:45:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-07T16:45:00Z"), Instant.parse("2020-07-07T17:00:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-07T17:00:00Z"), Instant.parse("2020-07-07T17:15:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-07T17:15:00Z"), Instant.parse("2020-07-07T17:30:00Z")),
            -1,
            false));

    MockHttpServletRequest getRequest = new MockHttpServletRequest(context);
    getRequest.addParameter("timeZoneOffset", "0");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    System.out.println(getRequest.getAttribute("weekList"));
    Assert.assertTrue(true);
  }
}