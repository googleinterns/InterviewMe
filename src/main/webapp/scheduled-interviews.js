// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


function onScheduledInterviewsLoad() {
  supplyLogoutLink();
  listScheduledInterviews(); 
}

// Retrieves the List of scheduled interviews from Datastore and builds the scheduled Interview section
function listScheduledInterviews() {
  let email = 'gswe@gmail.com'; 
  const scheduledInterviewsSection = document.getElementById('scheduled-interviews-cards');
  fetch(`/scheduled-interviews?userEmail=${email}`)
    .then(response => response.json())
    .then((scheduledInterviews) => {
      if (!isEmptyObject(scheduledInterviews)) {
        document.getElementById('no-scheduled-interviews').style.display = 'none'; 
        scheduledInterviews.forEach((scheduledInterview) => {
          scheduledInterviewsSection.appendChild(createScheduledInterviewCard(email, scheduledInterview)); 
        })
      }
    }); 
}

// Creates a scheduledInterview card using the fields of a scheduledInterview object
function createScheduledInterviewCard(email, scheduledInterview) {
  // Check the role of the current user for the scheduledInterview
  let role;  
  if(email === scheduledInterview.interviewerEmail) {
    role = 'Interviewer'; 
  } else {
    role = 'Interviewee';
  }
  const scheduledInterviewElement = document.createElement('div');
  scheduledInterviewElement.className = 'row';

  const scheduledInterviewCard = document.createElement('div');
  scheduledInterviewCard.className = 'card w-75 scheduled-interview-card';

  const scheduledInterviewCardBody = document.createElement('div');
  scheduledInterviewCardBody.className = 'card-body';

  const scheduledInterviewCardTitle = document.createElement('h5');
  scheduledInterviewCardTitle.className = 'card-title'; 
  scheduledInterviewCardTitle.innerText = `Your role: ${role}`;

  const scheduledInterviewCardText = document.createElement('p');
  scheduledInterviewCardText.className = 'card-text'; 
  scheduledInterviewCardText.innerText = createDateString(scheduledInterview); 

  const scheduledInterviewCardAttendees = document.createElement('ul'); 
  scheduledInterviewCardAttendees.className = 'list-group list-group-flush'; 

  const scheduledInterviewCardInterviewee = document.createElement('li'); 
  scheduledInterviewCardInterviewee.className = 'list-group-item'; 
  scheduledInterviewCardInterviewee.innerText = `Interviewee Email: ${scheduledInterview.intervieweeEmail}`; 

  const scheduledInterviewCardInterviewer = document.createElement('li'); 
  scheduledInterviewCardInterviewer.className = 'list-group-item'; 
  scheduledInterviewCardInterviewer.innerText = `Interviewer Email: ${scheduledInterview.interviewerEmail}`;

  scheduledInterviewCardAttendees.appendChild(scheduledInterviewCardInterviewee); 
  scheduledInterviewCardAttendees.appendChild(scheduledInterviewCardInterviewer); 
  scheduledInterviewCardBody.appendChild(scheduledInterviewCardTitle); 
  scheduledInterviewCardBody.appendChild(scheduledInterviewCardText); 
  scheduledInterviewCard.appendChild(scheduledInterviewCardBody); 
  scheduledInterviewCard.appendChild(scheduledInterviewCardAttendees);
  scheduledInterviewElement.appendChild(scheduledInterviewCard); 
  return scheduledInterviewElement; 
}

function isEmptyObject(obj) {
  if (obj.length && obj.length > 0)
      return false;          

  if (obj.length === 0)
     return true;           
}

function createDateString(scheduledInterview) { 
  var start = new Date();
  var end = new Date(); 
  start.setTime(scheduledInterview.when.start.seconds*1000);
  end.setTime(scheduledInterview.when.end.seconds*1000);
  return start.toLocaleDateString() + ' from ' + start.toLocaleTimeString() + ' to ' + end.toLocaleTimeString();
}
