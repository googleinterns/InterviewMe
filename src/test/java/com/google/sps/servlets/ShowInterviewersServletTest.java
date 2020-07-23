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

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.Availability;
import com.google.sps.data.FakeAvailabilityDao;
import com.google.sps.data.FakePersonDao;
import com.google.sps.data.Person;
import com.google.sps.data.TimeRange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;

@RunWith(JUnit4.class)
public final class ShowInterviewersServletTest {
  LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
  private FakeAvailabilityDao availabilityDao;
  private FakePersonDao personDao;

  private final String person1Email = "person1@mail";
  private final String person1Id = String.format("%d", person1Email.hashCode());
  private final Person person1 =
      Person.create(person1Id, person1Email, "Test", "Subject", "Google", "SWE", "linkedIn");

  private final String person2Email = "person2@mail";
  private final String person2Id = String.format("%d", person2Email.hashCode());
  private final Person person2 =
      Person.create(person2Id, person2Email, "Test", "Subject", "Google", "PM", "linkedIn");

  @Before
  public void setUp() {
    helper.setUp();
    availabilityDao = new FakeAvailabilityDao();
    personDao = new FakePersonDao();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void onlyReturnsInterviewersWhoAreAvailableForTheFullHour()
      throws IOException, ServletException {
    personDao.create(person1);
    ShowInterviewersServlet servlet = new ShowInterviewersServlet();
    servlet.init(availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");

    availabilityDao.create(
        Availability.create(
            person1.id(),
            new TimeRange(
                Instant.parse("2020-07-07T13:30:00Z"), Instant.parse("2020-07-07T13:45:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            person1.id(),
            new TimeRange(
                Instant.parse("2020-07-07T13:45:00Z"), Instant.parse("2020-07-07T14:00:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            person1.id(),
            new TimeRange(
                Instant.parse("2020-07-07T14:00:00Z"), Instant.parse("2020-07-07T14:15:00Z")),
            -1,
            false));

    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("utc", "2020-07-07T13:30:00Z");
    getRequest.addParameter("date", "Tuesday 7/7");
    getRequest.addParameter("time", "1:30 PM - 2:30 PM");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<Person> actual = (List<Person>) getRequest.getAttribute("interviewers");
    Assert.assertEquals(Arrays.asList(), actual);
  }

  @Test
  public void onlyReturnsInterviewersWhoAreNotScheduled() throws IOException, ServletException {
    personDao.create(person1);
    ShowInterviewersServlet servlet = new ShowInterviewersServlet();
    servlet.init(availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");

    availabilityDao.create(
        Availability.create(
            person1.id(),
            new TimeRange(
                Instant.parse("2020-07-07T13:30:00Z"), Instant.parse("2020-07-07T13:45:00Z")),
            -1,
            true));

    availabilityDao.create(
        Availability.create(
            person1.id(),
            new TimeRange(
                Instant.parse("2020-07-07T13:45:00Z"), Instant.parse("2020-07-07T14:00:00Z")),
            -1,
            true));

    availabilityDao.create(
        Availability.create(
            person1.id(),
            new TimeRange(
                Instant.parse("2020-07-07T14:00:00Z"), Instant.parse("2020-07-07T14:15:00Z")),
            -1,
            true));

    availabilityDao.create(
        Availability.create(
            person1.id(),
            new TimeRange(
                Instant.parse("2020-07-07T14:15:00Z"), Instant.parse("2020-07-07T14:30:00Z")),
            -1,
            true));

    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("utc", "2020-07-07T13:30:00Z");
    getRequest.addParameter("date", "Tuesday 7/7");
    getRequest.addParameter("time", "1:30 PM - 2:30 PM");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<Person> actual = (List<Person>) getRequest.getAttribute("interviewers");
    Assert.assertEquals(Arrays.asList(), actual);
  }

  @Test
  public void noSchedulingWithYourself() throws IOException, ServletException {
    ShowInterviewersServlet servlet = new ShowInterviewersServlet();
    servlet.init(availabilityDao, personDao);
    String userEmail = "person@gmail.com";
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");

    String userId = String.format("%d", userEmail.hashCode());

    availabilityDao.create(
        Availability.create(
            userId,
            new TimeRange(
                Instant.parse("2020-07-07T13:30:00Z"), Instant.parse("2020-07-07T13:45:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            userId,
            new TimeRange(
                Instant.parse("2020-07-07T13:45:00Z"), Instant.parse("2020-07-07T14:00:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            userId,
            new TimeRange(
                Instant.parse("2020-07-07T14:00:00Z"), Instant.parse("2020-07-07T14:15:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            userId,
            new TimeRange(
                Instant.parse("2020-07-07T14:15:00Z"), Instant.parse("2020-07-07T14:30:00Z")),
            -1,
            false));

    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("utc", "2020-07-07T13:30:00Z");
    getRequest.addParameter("date", "Tuesday 7/7");
    getRequest.addParameter("time", "1:30 PM - 2:30 PM");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<Person> actual = (List<Person>) getRequest.getAttribute("interviewers");
    Assert.assertEquals(Arrays.asList(), actual);
  }

  @Test
  public void successfulRun() throws IOException, ServletException {
    personDao.create(person2);
    ShowInterviewersServlet servlet = new ShowInterviewersServlet();
    servlet.init(availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");

    availabilityDao.create(
        Availability.create(
            person2.id(),
            new TimeRange(
                Instant.parse("2020-07-07T13:30:00Z"), Instant.parse("2020-07-07T13:45:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            person2.id(),
            new TimeRange(
                Instant.parse("2020-07-07T13:45:00Z"), Instant.parse("2020-07-07T14:00:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            person2.id(),
            new TimeRange(
                Instant.parse("2020-07-07T14:00:00Z"), Instant.parse("2020-07-07T14:15:00Z")),
            -1,
            false));

    availabilityDao.create(
        Availability.create(
            person2.id(),
            new TimeRange(
                Instant.parse("2020-07-07T14:15:00Z"), Instant.parse("2020-07-07T14:30:00Z")),
            -1,
            false));

    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("utc", "2020-07-07T13:30:00Z");
    getRequest.addParameter("date", "Tuesday 7/7");
    getRequest.addParameter("time", "1:30 PM - 2:30 PM");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<Person> actual = (List<Person>) getRequest.getAttribute("interviewers");
    List<Person> expected = new ArrayList<Person>();
    expected.add(person2);
    Assert.assertEquals(expected, actual);
  }
}
