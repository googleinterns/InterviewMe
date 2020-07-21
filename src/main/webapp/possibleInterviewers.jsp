<%@ page import="java.util.List" %>
<%@ page import="com.google.sps.data.Person" %>
<%
  List<Person> list = (List<Person>) request.getAttribute("interviewers");
  pageContext.setAttribute("list", list);
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
    <c:forEach items = "${pageScope.list}" var = "interviewer">
      <tr>
        <td>${interviewer.company()}</td>
        <td>${interviewer.job()}</td>
        <td>
          <button type="button" class="btn btn-primary" 
          data-company="${interviewer.company()}" data-job="${interviewer.job()}" 
          data-email="${interviewer.email()}" onclick="selectInterview(this)">
            Select
          </button>
        </td>
      </tr>
    </c:forEach>
    <!-- HARDCODED -->
     <tr>
        <td>Google</td>
        <td>Software Engineer</td>
        <td> 
          <button type="button" class="btn btn-primary" data-company="Google" 
          data-job="Software Engineer" data-email="gswe@gmail.com" 
          onclick="selectInterview(this)">
            Select
          </button>
        </td>
     </tr>
  </tbody>
</table>
