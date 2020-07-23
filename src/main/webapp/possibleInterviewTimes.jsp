<%@ page import="java.util.List" %>
<%@ page import="com.google.sps.data.PossibleInterviewSlot" %>
<%
  List<List<PossibleInterviewSlot>> list = (List<List<PossibleInterviewSlot>>) request.getAttribute("weekList");
  pageContext.setAttribute("list", list);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:forEach items = "${pageScope.list}" var = "day">
  <form>
    <div class="form-row">
      <div class="col-3 date-label">
        <label>${day.get(0).date()}</label>
      </div>
      <div class="col-5">
        <select class="form-control" id="${day.get(0).date()}">
          <c:forEach items = "${day}" var = "slot">
            <option value="${slot.utcEncoding()}" data-date="${slot.date()}">
              ${slot.time()}
            </option>
          </c:forEach>
        </select>
      </div>
      <div class="col-4">
        <button type="button" class="btn btn-primary mb-2"
          onclick="showInterviewers(this)" data-date="${day.get(0).date()}">
          Select
        </button>
      </div>
    </div>
  </form>
  <br>
</c:forEach>
