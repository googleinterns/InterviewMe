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
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolution;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Handles all things Google Calendar (for now just getting a Meet link).
public class CalendarAccess {
  private Calendar service;
  private static final String CALENDAR_ID = "info@jqed.dev";

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
  }

  public String getMeetLink(ScheduledInterview interview)
      throws IOException, GeneralSecurityException {
    Event event =
        new Event()
            .setSummary("Interview")
            .setDescription(
                "This event won't be shown to users, just used to \"reserve\" a Meet link.");

    DateTime startDateTime = new DateTime(interview.when().start().toString());
    EventDateTime start =
        new EventDateTime().setDateTime(startDateTime).setTimeZone("America/Toronto");
    event.setStart(start);

    DateTime endDateTime = new DateTime(interview.when().end().toString());
    EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("America/Toronto");
    event.setEnd(end);

    CreateConferenceRequest createRequest = new CreateConferenceRequest();
    createRequest.setRequestId("randomstring");
    createRequest.setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet"));
    event.setConferenceData(new ConferenceData().setCreateRequest(createRequest));

    event = service.events().insert(CALENDAR_ID, event).setConferenceDataVersion(1).execute();
    return event.getConferenceData().getEntryPoints().get(0).getUri();
  }
}
