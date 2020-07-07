<%@ page import="com.google.sps.data.AvailabilityTimeSlotGenerator,java.util.List,com.google.sps.data.AvailabilityTimeSlot,java.time.Instant" %>
<%
  List<AvailabilityTimeSlot> list = AvailabilityTimeSlotGenerator.timeSlotsForDay(Instant.now(), request.getParameter("timeZoneOffset"));
  pageContext.setAttribute("list", list);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<table class="table table-sm text-center">
  <thead>
    <tr>
      <th scope="col">${list.get(0).date()}</th>
    </tr>
  </thead>
  <tbody>
    <!-- TODO: Allow clicking and scrolling over multiple slots to select them.-->
    <c:forEach items = "${pageScope.list}" var = "timeSlot">
      <tr>
        <td onclick="toggleTile(this)" data-utc="${timeSlot.utcEncoding()}" class="${timeSlot.selected() ? 'table-success' : ''}">
          ${timeSlot.time()}
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>
