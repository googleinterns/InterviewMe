<%@ page import="com.google.sps.data.AvailabilityTimeSlots" %>
<%
  AvailabilityTimeSlots list = new AvailabilityTimeSlots(request.getParameter("timeZoneOffset"));
  pageContext.setAttribute("list", list);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 

<table class="table table-sm text-center">
  <thead>
    <tr>
      <th scope="col">
        ${list.getTimeSlots().get(0).getDate()}
        <!--<c:forEach items = "${pageScope.list.getTimeSlots()}" var = "timeSlot">
          "${timeSlot.getDate()}"</c:forEach>--></th>
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
