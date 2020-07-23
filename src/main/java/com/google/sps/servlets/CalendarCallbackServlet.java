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

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeCallbackServlet;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Utils;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import java.security.GeneralSecurityException;
import java.io.File;
import com.google.api.client.util.store.FileDataStoreFactory;
import java.security.GeneralSecurityException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.CalendarAccess;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

@WebServlet("/calendar-callback")
public class CalendarCallbackServlet extends AbstractAppEngineAuthorizationCodeCallbackServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
    System.out.println("success");
    try {
      credential.setExpiresInSeconds(Long.MAX_VALUE);
      Credential rsc = getStoredCredential(credential);
      System.out.println(rsc.getExpiresInSeconds()); // diffcheck this

      // CalendarAccess.addEvent(rsc);
    } catch (Exception e) { // GeneralSecurityException, ClassNotFoundException
    }
    resp.sendRedirect("/");
  }

  private Credential getStoredCredential(Credential credential)
      throws IOException, ClassNotFoundException {
    System.out.println("getStoredCredential");

    byte[] barr = serialize(credential);
    System.out.println(Arrays.toString(barr)); // diffcheck this
    Object obj = deserialize(barr);
    Credential rsc = (Credential) obj;
    return rsc;
  }

  public static byte[] serialize(Object obj) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(out);
    os.writeObject(obj);
    return out.toByteArray();
  }

  public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
    ByteArrayInputStream in = new ByteArrayInputStream(data);
    ObjectInputStream is = new ObjectInputStream(in);
    return is.readObject();
  }

  @Override
  public void onError(
      HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
      throws ServletException, IOException {
    String nickname = UserServiceFactory.getUserService().getCurrentUser().getNickname();
    resp.getWriter()
        .print(
            nickname
                + ", Calendar Authentication had an error "
                + errorResponse.getErrorDescription());
    resp.setStatus(200);
    resp.addHeader("Content-Type", "text/html");
  }

  @Override
  public String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    return Utils.getRedirectUri(req);
  }

  @Override
  public AuthorizationCodeFlow initializeFlow() throws IOException {
    return Utils.newFlow();
  }
}
