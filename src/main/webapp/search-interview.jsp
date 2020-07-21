<html>
  <head>
    <meta charset="UTF-8">
    <title>Search for an Interview</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css"
      integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous">
    <link rel="stylesheet" href="global.css">
    <link rel="stylesheet" href="search-interview.css">
    <script src="https://code.jquery.com/jquery-3.4.1.slim.min.js"
      integrity="sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js"
      integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js"
      integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>
    <script src="search-interview.js"></script>
    <script src="authentication.js"></script>
  </head>
  <body onload="onSearchInterviewLoad()">
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
            <a class="nav-item nav-link" href="availability.html">Availability</a>
            <a class="nav-item nav-link active" href="#">Scheduling</a>
            <!--TODO: Move this to the right of the navbar-->
            <a class="nav-item nav-link" href="#" id="login-tab">Logout</a>
          </div>
        </div>
      </nav>
      <br>
      <h1 class="text-center">Search for an Interview</h1>
      <h5 class="text-center lead">Search for possible times in which you will be interviewed!</h5>
      <br>
      <form>
        <div class="form-group">
          <!-- TODO: add parameters for this form. -->
          <div id="search-button-container">
            <button type="button" class="btn btn-primary mb-2" onclick="loadInterviews()">Search</button>
          </div>
        </div>
      </form>
      <br>      
      <section hidden id="search-results">
        <h1 class="text-center">Available Interview Times</h1>
        <h5 class="text-center lead">Select the time you want from the dropdown on the day you want. </h5>
        <br>
        <!--TODO: Include Timezone consideration; Add pagination; Make this into a jsp page-->
        <div id = "jsp-container"></div>
        <form>
          <div class="form-row">
            <div class="col-3 date-label">
              <label>Monday 6/29</label>
            </div>
            <div class="col-5">
              <select class="form-control">
                <option>8:15 AM - 9:15 AM</option>
                <option>10:30 AM - 11:30 AM</option>
                <option>1:00 PM - 2:00 PM</option>
              </select>
            </div>
            <div class="col-4">
              <button type="button" class="btn btn-primary mb-2">Select</button>
            </div>
          </div>
        </form>
        <br>
        <form>
          <div class="form-row">
            <div class="col-3 date-label">
              <label>Wednesday 7/1</label>
            </div>
            <div class="col-5">
              <select class="form-control">
                <option>6:30 PM - 7:30 PM</option>
              </select>
            </div>
            <div class="col-4">
              <button type="button" class="btn btn-primary mb-2">Select</button>
            </div>
          </div>
        </form>
        <br>     
        <form>
          <div class="form-row">
            <div class="col-3 date-label">
              <label>Thursday 7/2</label>
            </div>
            <div class="col-5">
              <select class="form-control">
                <option>9:30 AM - 10:30 AM</option>
                <option>6:30 PM - 7:30 PM</option>
              </select>
            </div>
            <div class="col-4">
              <button type="button" class="btn btn-primary mb-2">Select</button>
            </div>
          </div>
        </form>
        <br>  
        <form>
          <!-- TODO: Dynamically generate these with Sunday as the model. -->
          <!-- TODO: Make the week Sunday - Saturday. -->
          <div class="form-row">
            <div class="col-3 date-label">
              <label>Sunday 7/5</label>
            </div>
            <div class="col-5">
              <select class="form-control" id="7/5/2020-options">
                <option>9:30 AM - 10:30 AM</option>
                <option>6:30 PM - 7:30 PM</option>
              </select>
            </div>
            <div class="col-4">
              <button type="button" class="btn btn-primary mb-2" 
                onclick="showInterviewers(this)" id="7/5/2020">
                Select
              </button>
            </div>
          </div>
        </form>
        <br>  
      </section>
      <div class="modal fade" id="interviewer-modal" role="dialog">
        <div class="modal-dialog mw-100 w-25" role="document">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="modal-title"></h5>
              <button type="button" class="close" data-dismiss="modal">
                <span aria-hidden="true">&times;</span>
              </button>
            </div>
            <div class="modal-body" id="modal-body"></div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
          </div>
        </div>
      </div>
      <br><br><br>  
    </div>
  </body>
</html>
