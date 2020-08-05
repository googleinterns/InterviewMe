<%@ page import="com.google.sps.data.AvailabilityTimeSlotGenerator" %>
<%@ page import="com.google.sps.data.AvailabilityTimeSlot" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.Instant" %>
<%@ page import="com.google.sps.data.DatastoreAvailabilityDao" %>
<%@ page import="java.time.temporal.ChronoUnit" %>
<%@ page import="java.lang.Integer" %>
<%
  int timeZoneOffset = Integer.parseInt(request.getParameter("timeZoneOffset"));
  int daysOffset = Integer.parseInt(request.getParameter("page")) * 7;
  List<List<AvailabilityTimeSlot>> list = 
      AvailabilityTimeSlotGenerator.timeSlotsForWeek(
          Instant.now().plus(daysOffset, ChronoUnit.DAYS), 
          timeZoneOffset, 
          new DatastoreAvailabilityDao());
  pageContext.setAttribute("list", list);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<table class="table table-sm text-center">
  <thead>
    <tr>
      <th scope="col"></th>
      <c:forEach items = "${pageScope.list}" var = "day">
        <th scope="col">${day.get(0).date()}</th>
      </c:forEach>
    </tr>
  </thead>
  <tbody>
    <!-- TODO: Allow clicking and scrolling over multiple slots to select them.-->
    <c:forEach var = "i" begin = "0" end = "${pageScope.list.get(0).size() - 1}">
      <c:choose>
        <c:when test="${i%4 == 0}">
          <tr class='tall'>
            <td>${pageScope.list.get(0).get(i).time()}</td>
            <c:forEach items = "${pageScope.list}" var = "day">
              <td onclick="toggleTile(this)" data-utc="${day.get(i).utcEncoding()}" 
                  class="${day.get(i).getClassList()}">
              </td>
            </c:forEach>
          </tr>
        </c:when>
        <c:otherwise>
          <tr class='short'>
            <td></td>
            <c:forEach items = "${pageScope.list}" var = "day">
              <td onclick="toggleTile(this)" data-utc="${day.get(i).utcEncoding()}" 
                  class="${day.get(i).getClassList()}">
              </td>
            </c:forEach>
          </tr>
        </c:otherwise>
      </c:choose>
    </c:forEach>
  </tbody>
</table>
