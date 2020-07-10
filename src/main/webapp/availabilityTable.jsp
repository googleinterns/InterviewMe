<%@ page import="com.google.sps.data.AvailabilityTimeSlotGenerator" %>
<%@ page import="com.google.sps.data.AvailabilityTimeSlot" %>
<%@ page import="java.util.List,java.time.Instant" %>
<%@ page import="java.lang.Integer" %>
<%
  List<List<AvailabilityTimeSlot>> list = AvailabilityTimeSlotGenerator
    .timeSlotsForWeek(Instant.now(), Integer.parseInt(request
    .getParameter("timeZoneOffset")));
  pageContext.setAttribute("list", list);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<table class="table table-sm text-center">
  <thead>
    <tr>
      <c:forEach items = "${pageScope.list}" var = "day">
        <th scope="col">${day.get(0).date()}</th>
      </c:forEach>
    </tr>
  </thead>
  <tbody>
    <!-- TODO: Allow clicking and scrolling over multiple slots to select them.-->
    <!-- TODO: Change page format so that it is vertically condensed.-->
    <tr>
      <td id="first-slot" onclick="toggleTile(this)" 
        data-utc="${list.get(0).get(0).utcEncoding()}" 
        class="${list.get(0).get(0).selected() ? 'table-success' : ''}">
        ${list.get(0).get(0).time()}
      </td>
      <c:forEach var = "i" begin = "1" end = "6">
        <td onclick="toggleTile(this)" data-utc="${list.get(i).get(0).utcEncoding()}" 
          class="${list.get(i).get(0).selected() ? 'table-success' : ''}">
          ${list.get(i).get(0).time()}
        </td>
      </c:forEach>
    </tr>
    <c:forEach var = "i" begin = "1" end = "${pageScope.list.get(0).size() - 2}">
      <tr>
        <c:forEach items = "${pageScope.list}" var = "day">
          <td onclick="toggleTile(this)" data-utc="${day.get(i).utcEncoding()}" 
            class="${day.get(i).selected() ? 'table-success' : ''}">
            ${day.get(i).time()}
          </td>
        </c:forEach>
      </tr>
    </c:forEach>
    <tr>
      <c:forEach var = "i" begin = "0" end = "5">
        <td onclick="toggleTile(this)" 
          data-utc="${list.get(i).get(list.get(0).size() - 1).utcEncoding()}" 
          class="${list.get(i).get(list.get(0).size() - 1).selected() ? 'table-success' : ''}">
          ${list.get(i).get(list.get(0).size() - 1).time()}
        </td>
      </c:forEach>
      <td id="last-slot" onclick="toggleTile(this)" 
        data-utc="${list.get(6).get(list.get(0).size() - 1).utcEncoding()}" 
        class="${list.get(6).get(list.get(0).size() - 1).selected() ? 'table-success' : ''}">
        ${list.get(6).get(list.get(0).size() - 1).time()}
      </td>
    </tr>
  </tbody>
</table>
