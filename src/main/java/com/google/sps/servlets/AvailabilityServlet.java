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

import com.google.gson.Gson;
import com.google.sps.data.AvailabilityJSONConverter;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/availability")
public class AvailabilityServlet extends HttpServlet {
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("MADE IT TO AVAILABILITY SERVLET");
    Gson gson = new Gson();
    System.out.println("Before being converted to JSON: " + request.getParameter("body")); 
    // This is null. Need different method for converting request to JSON.
    String jsonRequest = gson.toJson(request.getParameter("body"));
    System.out.println("Here's the json from the request body: " + jsonRequest);
    // System.out.println(gson.fromJson(jsonRequest, AvailabilityJSONConverter.class));
  }
}
