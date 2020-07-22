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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class for JDO persistence, OAuth flow helpers, and others.
 *
 * @author Yaniv Inbar
 */
public class Utils {

  /**
   * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
   * globally shared instance across your application.
   */
  private static final AppEngineDataStoreFactory DATA_STORE_FACTORY =
      AppEngineDataStoreFactory.getDefaultInstance();

  /** Global instance of the HTTP transport. */
  static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();

  /** Global instance of the JSON factory. */
  static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static final String APPLICATION_NAME = "Interview Me";

  private static GoogleClientSecrets clientSecrets = null;

  static GoogleClientSecrets getClientCredential() throws IOException {
    // TODO: somehow remember who authorized in the past so they don't have to authorize again
    // TODO: Get these credentials in a deployment situation
    if (clientSecrets == null) {
      // System.out.println("Util getClientCredential is null");
      clientSecrets =
          GoogleClientSecrets.load(
              JSON_FACTORY,
              new InputStreamReader(
                  Utils.class.getResourceAsStream("/application_default_credentials.json")));
      Preconditions.checkArgument(
          !clientSecrets.getDetails().getClientId().startsWith("Enter ")
              && !clientSecrets.getDetails().getClientSecret().startsWith("Enter "),
          "Download client_secrets.json file from https://code.google.com/apis/console/"
              + "?api=calendar into calendar-appengine-sample/src/main/resources/client_secrets.json");
    }
    // System.out.println("Util getClientCredential is not null");
    return null;
  }

  public static String getRedirectUri(HttpServletRequest req) {
    // TODO: change this to the where the request was sent from
    GenericUrl url =
        new GenericUrl(
            "https://8080-cd144138-6164-4978-b5a9-12129253ea46.us-central1.cloudshell.dev");
    url.setRawPath("/calendar-callback");
    return url.build();
  }

  public static GoogleAuthorizationCodeFlow newFlow() throws IOException {
    return new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            getClientCredential(),
            Collections.singleton(CalendarScopes.CALENDAR))
        .setDataStoreFactory(DATA_STORE_FACTORY)
        .setAccessType("online")
        .build();
  }

  public static Calendar loadCalendarClient() throws IOException {
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    Credential credential = newFlow().loadCredential(userId);
    System.out.println("Util loadCalendarClient");
    return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  /**
   * Returns an {@link IOException} (but not a subclass) in order to work around restrictive GWT
   * serialization policy.
   */
  static IOException wrappedIOException(IOException e) {
    if (e.getClass() == IOException.class) {
      return e;
    }
    return new IOException(e.getMessage());
  }

  private Utils() {}
}
