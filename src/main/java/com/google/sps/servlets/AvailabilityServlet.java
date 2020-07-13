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
import java.io.BufferedReader;
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
    BufferedReader reader = request.getReader();
    StringBuffer buffer = new StringBuffer();
    String timestamps = null;

    while ((timestamps = reader.readLine()) != null) buffer.append(timestamps);
    String jsonString = buffer.toString();
    Gson gson = new Gson();
    AvailabilityJSONConverter utcEncodings =
        gson.fromJson(jsonString, AvailabilityJSONConverter.class);

    System.out.println("DEBUG: First Time Slot = " + utcEncodings.firstSlotUTC());
    System.out.println("DEBUG: Last Time Slot = " + utcEncodings.lastSlotUTC());
    System.out.println("DEBUG: Selected Time Slots = " + utcEncodings.selectedSlotsUTC());
  }
}
