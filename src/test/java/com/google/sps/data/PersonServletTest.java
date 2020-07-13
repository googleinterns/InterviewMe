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

import com.google.sps.servlets.PersonServlet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/** */
@RunWith(JUnit4.class)
public final class PersonServletTest {

  //
  @Test
  public void postTwo() throws IOException, UnsupportedEncodingException {

    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    postRequest.addParameter("user-email", "a@gmail.com");
    postRequest.addParameter("first-name", "a");
    postRequest.addParameter("last-name", "a");
    postRequest.addParameter("company", "");
    postRequest.addParameter("job", "");
    postRequest.addParameter("linkedin", "");

    PersonServlet personServlet = new PersonServlet();
    personServlet.init(new FakePersonDao());
    personServlet.doPost(postRequest, new MockHttpServletResponse());

    postRequest.setParameter("user-email", "b@gmail.com");
    postRequest.addParameter("first-name", "b");
    postRequest.addParameter("last-name", "b");
    personServlet.doPost(postRequest, new MockHttpServletResponse());

    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("user-email", "b@gmail.com");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personServlet.doGet(getRequest, getResponse);

    System.out.println(getResponse.getContentAsString());
  }
}
