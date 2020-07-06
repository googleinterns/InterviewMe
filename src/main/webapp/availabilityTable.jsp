<%@ page import="com.google.sps.data.AvailabilityTimeSlots" %>
<%
  AvailabilityTimeSlots list = new AvailabilityTimeSlots(request.getParameter("timeZoneOffset"));
%>

<table class="table table-sm text-center">
  <thead>
    <tr>
      <th scope="col">Monday 6/29</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td onclick="switchTile(this)">9:00 AM</td>
    </tr>
    <tr>
      <td onclick="switchTile(this)">9:15 AM</td>
    </tr>
    <tr>
      <td onclick="switchTile(this)">9:30 AM</td>
    </tr>
    <tr>
      <td onclick="switchTile(this)">9:45 AM</td>
    </tr>
  </tbody>
</table>
