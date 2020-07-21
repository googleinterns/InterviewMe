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

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sendgrid.*;
import java.io.IOException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import com.google.sps.data.SecretHandler;

/** A way to run functions in the SecretHandler class. */
@RunWith(JUnit4.class)
public final class SecretHandlerTest {
  LocalServiceTestHelper helper;

  @Before
  public void setUp() {
    helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // A way to run secretHandler.getSecretValue() without having to mvn run the whole project.
  // Does not actually ensure it works.
  @Test
  public void basic() throws IOException, Exception {
    SecretHandler.getSecretValue("interview-me-step-2020", "SENDGRID_API_KEY");
  }
}
