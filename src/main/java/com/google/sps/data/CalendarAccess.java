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
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.*;
// import com.google.api.services.calendar.model.ConferenceSolution;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Handles all things Google Calendar (event adding primarily).
public class CalendarAccess {
  private Calendar service;
  private static final String CALENDAR_ID =
      "info@jqed.dev"; // also the person we're trying to impersonate

  // TODO: remember to write tests in the code that calls CalendarAccess() that handle what happens
  // per each exception
  public CalendarAccess()
      throws GeneralSecurityException, IOException, URISyntaxException, Exception {
    String key = new SecretFetcher("interview-me-step-2020").getSecretValue("SERVICE_ACCT_KEY");
    GoogleCredential credential =
        GoogleCredential.fromStream(new ByteArrayInputStream(key.getBytes()))
            .createScoped(Collections.singletonList(CalendarScopes.CALENDAR));

    service =
        new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
            .setApplicationName("Interview Me CalendarAccess")
            .build();
    System.out.println("calendarService built");
  }

  public void listNextTen() throws IOException {
    System.out.println("listNextTen");
    System.out.println(service.calendars().get(CALENDAR_ID).execute());

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

  public void addEvent() throws IOException, GeneralSecurityException {
    System.out.println("addEvent");

    Event event =
        new Event()
            .setSummary("Google I/O 2015")
            .setLocation("800 Howard St., San Francisco, CA 94103")
            .setDescription("A chance to hear more about Google's developer products.");

    DateTime startDateTime = new DateTime("2020-07-31T09:00:00-07:00");
    EventDateTime start =
        new EventDateTime().setDateTime(startDateTime).setTimeZone("America/Toronto");
    event.setStart(start);
    System.out.printf("%s (%s)\n", event.getSummary(), start);

    DateTime endDateTime = new DateTime("2020-07-31T17:00:00-07:00");
    EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("America/Toronto");
    event.setEnd(end);

    CreateConferenceRequest createRequest = new CreateConferenceRequest();
    createRequest.setRequestId("randomstring1");
    createRequest.setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet"));

    event.setConferenceData(new ConferenceData().setCreateRequest(createRequest));

    event = service.events().insert(CALENDAR_ID, event).setConferenceDataVersion(1).execute();
    System.out.printf("Event created: %s\n", event);
    System.out.printf(
        "Meet link: %s\n", event.getConferenceData().getEntryPoints().get(0).getUri());
  }
}
