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
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeCallbackServlet;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OAuth2Callback extends AbstractAppEngineAuthorizationCodeCallbackServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
    resp.sendRedirect("/");
  }

  @Override
  public void onError(
      HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
      throws ServletException, IOException {
    String nickname = UserServiceFactory.getUserService().getCurrentUser().getNickname();
    resp.getWriter().print("<h3>" + nickname + ", why don't you want to play with me?</h1>");
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
