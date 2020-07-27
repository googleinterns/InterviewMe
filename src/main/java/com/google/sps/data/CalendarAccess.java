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
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.google.api.client.auth.oauth2.StoredCredential;
import java.io.BufferedReader;
import java.util.stream.Collectors;

import com.google.api.services.calendar.*;
import com.google.api.services.calendar.model.*;

public class CalendarAccess {
  private static final String APPLICATION_NAME = "Interview Me";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  /**
   * Global instance of the scopes required by this quickstart. If modifying these scopes, delete
   * your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

  // private static final String KEY_PATH =
  //     "src/main/resources/interview-me-step-2020-6ae1489eb3f9.json";
  // private static final String KEY_PATH_RUN = "interview-me-step-2020-6ae1489eb3f9.json";

  public CalendarAccess() {}

  public void getService() throws GeneralSecurityException, IOException {
    try {
      System.out.println("getService()");
      FileInputStream fstream =
          new FileInputStream(
              new File(
                  this.getClass()
                      .getResource("/interview-me-step-2020-627ae7fba2f5.json")
                      .toURI()));
      FileInputStream fstream2 =
          new FileInputStream(
              new File(
                  this.getClass()
                      .getResource("/interview-me-step-2020-627ae7fba2f5.json")
                      .toURI()));
      String result =
          new BufferedReader(new InputStreamReader(fstream2))
              .lines()
              .collect(Collectors.joining("\n"));
      System.out.println(result);
      GoogleCredential credential = GoogleCredential.fromStream(fstream).createScoped(SCOPES);
      // .createDelegated("interviewme.business@gmail.com");
      System.out.println("getServiceAccountUser: " + credential.getServiceAccountUser());

      final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      Calendar calendarService =
          new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
              .setApplicationName(APPLICATION_NAME)
              .build();
      System.out.println("calendarService built");
      listNextTen(calendarService);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  public static void listNextTen(Calendar service) throws IOException {
    System.out.println("listNextTen");
    System.out.println(service.calendars().get("interviewme.business@gmail.com").execute());
    // System.out.println(service.calendars().setMaxResults(10).execute());
    // List the next 10 events from the primary calendar.
    DateTime now = new DateTime(System.currentTimeMillis());
    Events events =
        service
            .events()
            .list("interviewme.business@gmail.com")
            .setMaxResults(10)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute();
    System.out.println("events made");
    List<Event> items = events.getItems();
    if (items.isEmpty()) {
      System.out.println("No upcoming events found.");
    } else {
      System.out.println("Upcoming events");
      for (Event event : items) {
        DateTime start = event.getStart().getDateTime();
        if (start == null) {
          start = event.getStart().getDate();
        }
        System.out.printf("%s (%s)\n", event.getSummary(), start);
      }
    }
  }

  public static void addEvent(Credential credential) throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Calendar service =
        new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    System.out.println("addEvent after service built");

    Event event =
        new Event()
            .setSummary("Google I/O 2015")
            .setLocation("800 Howard St., San Francisco, CA 94103")
            .setDescription("A chance to hear more about Google's developer products.");

    DateTime startDateTime = new DateTime("2020-07-23T09:00:00-07:00");
    EventDateTime start =
        new EventDateTime().setDateTime(startDateTime).setTimeZone("America/Toronto");
    event.setStart(start);
    System.out.printf("%s (%s)\n", event.getSummary(), start);

    DateTime endDateTime = new DateTime("2020-07-23T17:00:00-07:00");
    EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("America/Toronto");
    event.setEnd(end);

    EventAttendee[] attendees =
        new EventAttendee[] {
          new EventAttendee().setEmail("lpage@example.com"),
          new EventAttendee().setEmail("sbrin@example.com"),
        };
    event.setAttendees(Arrays.asList(attendees));
    System.out.printf("added attendees");

    EventReminder[] reminderOverrides =
        new EventReminder[] {
          new EventReminder().setMethod("email").setMinutes(24 * 60),
          new EventReminder().setMethod("popup").setMinutes(10),
        };
    Event.Reminders reminders =
        new Event.Reminders().setUseDefault(false).setOverrides(Arrays.asList(reminderOverrides));
    System.out.printf("added reminders");

    event.setReminders(reminders);

    String calendarId = "clairexy@google.com";
    // event = service.events().insert(calendarId, event).execute();
    // System.out.printf("Event created: %s\n", event.getHtmlLink());
  }
}
