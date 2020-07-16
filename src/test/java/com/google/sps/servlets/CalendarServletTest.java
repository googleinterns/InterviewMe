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

package com.google.sps.apis;

import org.junit.Assert;
import org.junit.After;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;
import java.security.GeneralSecurityException;
import com.google.sps.servlets.CalendarServlet;

/** */
@RunWith(JUnit4.class)
public final class CalendarServletTest {

  LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Tests whether or not two TimeRanges are the same
  @Test
  public void basic() throws IOException, GeneralSecurityException {
    // a is logged in.
    helper.setEnvIsLoggedIn(true).setEnvEmail("the.claire.yang@gmail.com").setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();

    CalendarServlet calServlet = new CalendarServlet();

    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    calServlet.doGet(getRequest, getResponse);
    System.out.println(getResponse.getRedirectedUrl());
  }
}
