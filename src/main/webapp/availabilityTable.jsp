<%@ page import="com.google.sps.data.AvailabilityTimeSlotGenerator" %>
<%
  AvailabilityTimeSlotGenerator list = new AvailabilityTimeSlotGenerator(request.getParameter("timeZoneOffset"));
  pageContext.setAttribute("list", list);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 

<table class="table table-sm text-center">
  <thead>
    <tr>
      <th scope="col">${list.getTimeSlots().get(0).date()}</th>
    </tr>
  </thead>
  <tbody>
    <!-- TODO: Allow clicking and scrolling over multiple slots to select them.-->
    <c:forEach items = "${pageScope.list.getTimeSlots()}" var = "timeSlot">
      <tr>
        <td onclick="switchTile(this)" data-utc="${timeSlot.utcEncoding()}" class="${timeSlot.selected() ? 'table-success' : ''}">
          ${timeSlot.time()}
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>
