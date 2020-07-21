<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@ page import="com.google.sps.data.ScheduledInterview" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
  <head>
    <meta charset="UTF-8">
    <title>Scheduled Interviews</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css"
      integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous">
    <link rel="stylesheet" href="scheduled-interviews.css">
    <script src="https://code.jquery.com/jquery-3.4.1.slim.min.js"
      integrity="sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js"
      integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js"
      integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>
    <script src="scheduled-interviews.js"></script>
    <script src="authentication.js"></script>
  </head>
  <body onload="onScheduledInterviewsLoad()">
    <div id="content">
      <nav class="navbar navbar-expand-lg navbar-light bg-light">
        <span class="navbar-brand mb-0 h1">InterviewMe</span>
        <button class="navbar-toggler" type="button" data-toggle="collapse" 
          data-target="#navbarNavAltMarkup" aria-controls="navbarNavAltMarkup" 
          aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNavAltMarkup">
          <div class="navbar-nav">
            <a class="nav-item nav-link" href="index.html">Home</a>
            <a class="nav-item nav-link" href="profile.html">Profile</a>
            <a class="nav-item nav-link active" href="#">Events</a>
            <a class="nav-item nav-link" href="availability.html">Availability</a>
            <a class="nav-item nav-link" href="search-interview.html">Scheduling</a>
            <!--TODO: Move this to the right of the navbar-->
            <a class="nav-item nav-link" href="#" id="login-tab">Logout</a>
          </div>
        </div>
      </nav>
      <br>
      <h1 class="text-center">Scheduled Interviews</h1>
      <section id="scheduled-interviews-cards">
      <!-- For each scheduled interview in the returned request, generate jsp scheduledInterviewCard.-->
      <c:choose>
        <c:when test= "${empty scheduledInterviews}">
          <h2 style="text-align: center">No Scheduled Interviews</h2>
        </c:when>
        <c:otherwise>
          <c:forEach items= "${scheduledInterviews}" var="scheduledInterview">
            <div class="row">
              <div class="card w-75 scheduled-interview-card">
                <div class="card-body">
                 <c:choose>
                    <c:when test="${scheduledInterview}.interviewerEmail().equals(${userEmail})">
                      <h5 class="card-title">Your role: Interviewer</h5>
                    </c:when>
                    <c:otherwise>
                      <h5 class="card-title">Your role: Interviewee</h5>
                    </c:otherwise>   
                  </c:choose>
                  <p class="card-text">${scheduledInterview}.when().start() - ${scheduledInterview}.when().end()</p>
                </div>
                <ul class="list-group list-group-flush">
                  <li class="list-group-item">Interviewee Email: ${scheduledInterview}.intervieweeEmail()</li>
                  <li class="list-group-item">Interviewer Email: ${scheduledInterview}.interviewerEmail()</li>
                </ul>
              </div>
            </div>
          </c:forEach>
        </c:otherwise>
      </c:choose>
      </section>
      <br><br><br>
    </div>
  </body>
</html>