<!--<!DOCTYPE html>-->
<html>
  <head>
    <meta charset="UTF-8">
    <title>Availability</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css"
      integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous">
    <link rel="stylesheet" href="global.css">
    <link rel="stylesheet" href="availability.css">
    <script src="https://code.jquery.com/jquery-3.4.1.slim.min.js"
      integrity="sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js"
      integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js"
      integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>
    <script src="availability.js"></script>
    <script src="logout.js"></script>
  </head>
  <body onload="onAvailabilityLoad()">
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
            <a class="nav-item nav-link" href="scheduled-interviews.html">Events</a>
            <a class="nav-item nav-link active" href="#">Availability</a>
            <a class="nav-item nav-link" href="search-interview.html">Scheduling</a>
            <!--TODO: Move this to the right of the navbar-->
            <a class="nav-item nav-link" href="#" id="login-tab">Logout</a>
          </div>
        </div>
      </nav>
      <br>
      <h1 class="text-center">Availability</h1>
      <h5 class="text-center lead">Update your availability to be an Interviewer for this week below:</h5>
      <br>
      <!--TODO: Include Timezone consideration; Check inputs for correctness: end is after start, at least an hour long;
         Add pagination; Figure out how to update the dates (dynamically); store and show inputted data; Allow for more than one
         time range per day (have a plus button)-->
         
      <!--TODO: Put entire table in a JSP file that will then be included here (meaning that this will need to be a jsp file too).-->
      <div id="table-container"></div>
      <div id="submit-button-container">
        <button type="submit" class="btn btn-primary mb-2 submit-button">Update Availability</button>
      </div>
      <br><br><br>
    </div>
  </body>
</html>
