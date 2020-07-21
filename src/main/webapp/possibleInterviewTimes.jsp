<%@ page import="java.util.List" %>
<%@ page import="com.google.sps.data.PossibleInterviewSlot" %>
<%
  List<List<PossibleInterviewSlot>> list = (List<List<PossibleInterviewSlot>>) request.getAttribute("weekList");
  pageContext.setAttribute("list", list);
  System.out.println("DEBUG: Made it to JSP.");
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h1>HI THERE!</h1>

<c:forEach items = "${pageScope.list}" var = "day">
  <form>
    <div class="form-row">
      <div class="col-3 date-label">
        <label>${day.get(0).date()}</label>
      </div>
      <div class="col-5">
        <select class="form-control">
          <c:forEach items = "${day}" var = "slot">
            <option value="${slot.utcEncoding()}" data-date="${slot.date()}">
              ${slot.time()}
            </option>
          </c:forEach>
        </select>
      </div>
      <div class="col-4">
        <!-- TODO: Create an onclick method that takes 'this' as a parameter.-->
        <button type="button" class="btn btn-primary mb-2">Select</button>
      </div>
    </div>
  </form>
  <br>
</c:forEach>
