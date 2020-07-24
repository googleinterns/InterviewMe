<%@ page import="java.util.List" %>
<%@ page import="com.google.sps.data.PossibleInterviewer" %>
<%
  Set<PossibleInterviewer> list = (Set<PossibleInterviewer>) request.getAttribute("interviewers");
  pageContext.setAttribute("set", set);
  String utc = request.getParameter("utc");
  pageContext.setAttribute("utc", utc);
  String time = request.getParameter("time");
  pageContext.setAttribute("time", time);
  String date = request.getParameter("date");
  pageContext.setAttribute("date", date);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<table class="table">
  <thead>
     <tr>
        <th scope="col">Company</th>
        <th scope="col">Job</th>
     </tr>
  </thead>
  <tbody>
    <c:forEach items = "${pageScope.set}" var = "interviewer">
      <tr>
        <td>${interviewer.company()}</td>
        <td>${interviewer.job()}</td>
        <td>
          <button type="button" class="btn btn-primary" 
          data-company="${interviewer.company()}" data-job="${interviewer.job()}" 
          data-utc="${pageScope.utc}" data-time="${pageScope.time}"
          data-date="${pageScope.date}" onclick="selectInterview(this)">
            Select
          </button>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>
