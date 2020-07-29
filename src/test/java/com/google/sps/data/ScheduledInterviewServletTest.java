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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.sps.data.FakePersonDao;
import com.google.sps.data.Job;
import com.google.sps.data.Person;
import com.google.sps.servlets.ScheduledInterviewServlet;
import com.google.sps.data.PutAvailabilityRequest;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;
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
import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public final class ScheduledInterviewServletTest {
  LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalCapabilitiesServiceTestConfig());
  private FakeScheduledInterviewDao scheduledInterviewDao;
  private FakeAvailabilityDao availabilityDao;
  private FakePersonDao personDao;

  private final Person GoogleSWE1 =
      Person.create(
          emailToId("user1@mail"),
          "user1@mail",
          "User",
          "Test",
          "Google",
          "SWE",
          "linkedIn",
          EnumSet.noneOf(Job.class));
  private final Availability GoogleSWE1Avail1 =
      Availability.create(
          GoogleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:00:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability GoogleSWE1Avail2 =
      Availability.create(
          GoogleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:00:00Z"), Instant.parse("2020-07-20T13:15:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability GoogleSWE1Avail3 =
      Availability.create(
          GoogleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:15:00Z"), Instant.parse("2020-07-20T13:30:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability GoogleSWE1Avail4 =
      Availability.create(
          GoogleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:30:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);

  private final Person GoogleSWE2 =
      Person.create(
          emailToId("user2@mail"),
          "user2@mail",
          "User",
          "Test",
          "Google",
          "SWE",
          "linkedIn",
          EnumSet.noneOf(Job.class));
  private final Availability GoogleSWE2Avail1 =
      Availability.create(
          GoogleSWE2.id(),
          new TimeRange(
              Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:00:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability GoogleSWE2Avail2 =
      Availability.create(
          GoogleSWE2.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:00:00Z"), Instant.parse("2020-07-20T13:15:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability GoogleSWE2Avail3 =
      Availability.create(
          GoogleSWE2.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:15:00Z"), Instant.parse("2020-07-20T13:30:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability GoogleSWE2Avail4 =
      Availability.create(
          GoogleSWE2.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:30:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);

  private final Person GooglePM =
      Person.create(
          emailToId("user3@mail"),
          "user3@mail",
          "User",
          "Test",
          "Google",
          "PM",
          "linkedIn",
          EnumSet.noneOf(Job.class));
  private final Availability GooglePMAvail1 =
      Availability.create(
          GooglePM.id(),
          new TimeRange(
              Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:00:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability GooglePMAvail2 =
      Availability.create(
          GooglePM.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:00:00Z"), Instant.parse("2020-07-20T13:15:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability GooglePMAvail3 =
      Availability.create(
          GooglePM.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:15:00Z"), Instant.parse("2020-07-20T13:30:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability GooglePMAvail4 =
      Availability.create(
          GooglePM.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:30:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);

  @Before
  public void setUp() {
    helper.setUp();
    scheduledInterviewDao = new FakeScheduledInterviewDao();
    availabilityDao = new FakeAvailabilityDao();
    personDao = new FakePersonDao();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Tests whether only the scheduled interviews that the current user is involved in are returned.
  @Test
  public void returnsScheduledInterviewsForUser() throws IOException {
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(scheduledInterviewDao, availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail(GoogleSWE1.email()).setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personDao.create(GoogleSWE1);
    personDao.create(GoogleSWE2);
    personDao.create(GooglePM);
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-05T18:00:00Z"), Instant.parse("2020-07-05T19:00:00Z")),
            GoogleSWE1.id(),
            GoogleSWE2.id()));
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-05T20:00:00Z"), Instant.parse("2020-07-05T21:00:00Z")),
            GoogleSWE2.id(),
            GooglePM.id()));
    getRequest.addParameter("timeZone", "America/New_York");
    scheduledInterviewServlet.doGet(getRequest, getResponse);
    List<ScheduledInterviewRequest> actual =
        (List<ScheduledInterviewRequest>) getRequest.getAttribute("scheduledInterviews");
    ScheduledInterviewRequest expectedInterview =
        new ScheduledInterviewRequest(
            actual.get(0).getId(),
            "Sunday, July 5, 2020 from 2:00 PM to 3:00 PM",
            GoogleSWE1.firstName(),
            GoogleSWE2.firstName(),
            "Interviewer");
    List<ScheduledInterviewRequest> expected = new ArrayList<ScheduledInterviewRequest>();
    expected.add(expectedInterview);
    Assert.assertEquals(expected, actual);
  }

  // Tests that the list of scheduledInterviews is in the correct order
  @Test
  public void returnsScheduledInterviewsInOrder() throws IOException {
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(scheduledInterviewDao, availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail(GoogleSWE1.email()).setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personDao.create(GoogleSWE1);
    personDao.create(GoogleSWE2);
    personDao.create(GooglePM);
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-05T18:00:00Z"), Instant.parse("2020-07-05T19:00:00Z")),
            GoogleSWE1.id(),
            GoogleSWE2.id()));
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-05T20:00:00Z"), Instant.parse("2020-07-05T21:00:00Z")),
            GoogleSWE1.id(),
            GoogleSWE2.id()));
    getRequest.addParameter("timeZone", "Etc/UCT");
    scheduledInterviewServlet.doGet(getRequest, getResponse);
    List<ScheduledInterviewRequest> actual =
        (List<ScheduledInterviewRequest>) getRequest.getAttribute("scheduledInterviews");
    ScheduledInterviewRequest scheduledInterview1 =
        new ScheduledInterviewRequest(
            actual.get(0).getId(),
            "Sunday, July 5, 2020 from 6:00 PM to 7:00 PM",
            GoogleSWE1.firstName(),
            GoogleSWE2.firstName(),
            "Interviewer");
    ScheduledInterviewRequest scheduledInterview2 =
        new ScheduledInterviewRequest(
            actual.get(1).getId(),
            "Sunday, July 5, 2020 from 8:00 PM to 9:00 PM",
            GoogleSWE1.firstName(),
            GoogleSWE2.firstName(),
            "Interviewer");
    List<ScheduledInterviewRequest> expected = new ArrayList<ScheduledInterviewRequest>();
    expected.add(scheduledInterview1);
    expected.add(scheduledInterview2);
    // Used assertThat in order to see what the actual field differences are
    assertThat(actual).containsExactlyElementsIn(expected);
  }

  // Tests whether a scheduledInterview object was added to datastore with one possible interviewer.
  @Test
  public void onlyOnePossibleInterviewer() throws IOException {
    personDao.create(GoogleSWE1);
    availabilityDao.create(GoogleSWE1Avail1);
    availabilityDao.create(GoogleSWE1Avail2);
    availabilityDao.create(GoogleSWE1Avail3);
    availabilityDao.create(GoogleSWE1Avail4);
    personDao.create(GooglePM);
    availabilityDao.create(GooglePMAvail1);
    availabilityDao.create(GooglePMAvail2);
    availabilityDao.create(GooglePMAvail3);
    availabilityDao.create(GooglePMAvail4);
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(scheduledInterviewDao, availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    String jsonString =
        "{\"company\":\"Google\",\"job\":\"SWE\",\"utcStartTime\":\"2020-07-20T12:45:00Z\"}";
    postRequest.setContent(jsonString.getBytes(StandardCharsets.UTF_8));
    scheduledInterviewServlet.doPost(postRequest, postResponse);
    List<ScheduledInterview> actual =
        scheduledInterviewDao.getScheduledInterviewsInRangeForUser(
            GoogleSWE1.id(),
            Instant.parse("2020-07-20T12:45:00Z"),
            Instant.parse("2020-07-20T13:45:00Z"));
    ScheduledInterview expected =
        ScheduledInterview.create(
            actual.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
            GoogleSWE1.id(),
            emailToId("user@company.org"));
    Assert.assertEquals(expected, actual.get(0));
  }

  // Tests that the selected interviewer is one of the possible interviewers.
  @Test
  public void picksOneOfThePossibleInterviewers() throws IOException {
    personDao.create(GoogleSWE1);
    availabilityDao.create(GoogleSWE1Avail1);
    availabilityDao.create(GoogleSWE1Avail2);
    availabilityDao.create(GoogleSWE1Avail3);
    availabilityDao.create(GoogleSWE1Avail4);
    personDao.create(GoogleSWE2);
    availabilityDao.create(GoogleSWE2Avail1);
    availabilityDao.create(GoogleSWE2Avail2);
    availabilityDao.create(GoogleSWE2Avail3);
    availabilityDao.create(GoogleSWE2Avail4);
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(scheduledInterviewDao, availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    String jsonString =
        "{\"company\":\"Google\",\"job\":\"SWE\",\"utcStartTime\":\"2020-07-20T12:45:00Z\"}";
    postRequest.setContent(jsonString.getBytes(StandardCharsets.UTF_8));
    scheduledInterviewServlet.doPost(postRequest, postResponse);
    List<ScheduledInterview> actual =
        scheduledInterviewDao.getScheduledInterviewsInRangeForUser(
            emailToId("user@company.org"),
            Instant.parse("2020-07-20T12:45:00Z"),
            Instant.parse("2020-07-20T13:45:00Z"));
    ScheduledInterview expected1 =
        ScheduledInterview.create(
            actual.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
            GoogleSWE1.id(),
            emailToId("user@company.org"));
    ScheduledInterview expected2 =
        ScheduledInterview.create(
            actual.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
            GoogleSWE2.id(),
            emailToId("user@company.org"));
    boolean actualIsExpectedOneOrTwo =
        actual.get(0).equals(expected1) || actual.get(0).equals(expected2);
    Assert.assertTrue(actualIsExpectedOneOrTwo);
  }

  // Tests that the availabilities for the involved parties are marked as scheduled.
  @Test
  public void availabilitiesAreScheduled() throws IOException {
    personDao.create(GoogleSWE1);
    availabilityDao.create(GoogleSWE1Avail1);
    availabilityDao.create(GoogleSWE1Avail2);
    availabilityDao.create(GoogleSWE1Avail3);
    availabilityDao.create(GoogleSWE1Avail4);
    availabilityDao.create(
        Availability.create(
            emailToId("user@company.org"),
            new TimeRange(
                Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:00:00Z")),
            /*id=*/ -1,
            false));
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(scheduledInterviewDao, availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    String jsonString =
        "{\"company\":\"Google\",\"job\":\"SWE\",\"utcStartTime\":\"2020-07-20T12:45:00Z\"}";
    postRequest.setContent(jsonString.getBytes(StandardCharsets.UTF_8));
    scheduledInterviewServlet.doPost(postRequest, postResponse);
    boolean allAvailabilitiesAreScheduled = true;
    List<Availability> affectedAvailabilityForInterviewer =
        availabilityDao.getInRangeForUser(
            GoogleSWE1.id(),
            Instant.parse("2020-07-20T12:45:00Z"),
            Instant.parse("2020-07-20T13:45:00Z"));
    List<Availability> affectedAvailabilityForInterviewee =
        availabilityDao.getInRangeForUser(
            emailToId("user@company.org"),
            Instant.parse("2020-07-20T12:45:00Z"),
            Instant.parse("2020-07-20T13:45:00Z"));
    List<Availability> allAffectedAvailability = new ArrayList<Availability>();
    allAffectedAvailability.addAll(affectedAvailabilityForInterviewer);
    allAffectedAvailability.addAll(affectedAvailabilityForInterviewee);
    for (Availability avail : allAffectedAvailability) {
      if (!avail.scheduled()) {
        allAvailabilitiesAreScheduled = false;
      }
    }
    Assert.assertTrue(allAvailabilitiesAreScheduled);
  }

  // Tests errors with Instant parsing.
  @Test
  public void invalidInstant() throws IOException {
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(scheduledInterviewDao, availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    // No dash between 07 and 20
    String jsonString =
        "{\"company\":\"Google\",\"job\":\"SWE\",\"utcStartTime\":\"2020-0720T12:45:00Z\"}";
    postRequest.setContent(jsonString.getBytes(StandardCharsets.UTF_8));
    scheduledInterviewServlet.doPost(postRequest, postResponse);
    Assert.assertEquals(400, postResponse.getStatus());
  }

  private String emailToId(String email) {
    return String.format("%d", email.hashCode());
  }
}
