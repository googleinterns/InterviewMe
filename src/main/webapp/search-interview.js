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

function onSearchInterviewLoad() {
  const loginInfo = getLoginInfo();
  loginInfo.then(supplyLogoutLinkOrRedirectHome); 
  loginInfo.then(getUserOrRedirectRegistration);
}

// Queries Datastore for available interview times and renders them on the
// page.
function loadInterviews() {
  const searchResultsDiv = document.getElementById("search-results");
  searchResultsDiv.removeAttribute("hidden");

  fetch(`/load-interviews?timeZoneOffset=${browserTimezoneOffset()}`)
  .then(response => response.text())
    .then(interviewTimes => {
      interviewTimesDiv().innerHTML = interviewTimes;
    });
}

function interviewTimesDiv() {
  return document.getElementById('interview-times-container');
}

function browserTimezoneOffset() {
  let date = new Date();
  return (-1) * date.getTimezoneOffset();
}

// Confirms interview selection with user and sends this selection to Datastore 
// if confirmed.
function selectInterview(interviewer) {
  if (confirm(
    // TOOD: fill in these times dynamically from ids in the .jsp file.
      `You selected: ${interviewer.getAttribute('data-date')} from ` +
      `${interviewer.getAttribute('data-time')} with a ` +
      `${interviewer.getAttribute('data-company')} ` +
      `${interviewer.getAttribute('data-job')}. ` +
      `Click OK if you wish to proceed.`)) {
    alert(
      `You have scheduled an interview on ${interviewer.getAttribute('data-date')}` +
      ` from ${interviewer.getAttribute('data-time')} ` +
      `with a ${interviewer.getAttribute('data-company')} ` +
      `${interviewer.getAttribute('data-job')}. Check your email for more ` +
      `information.`);
    // TODO: Call a servlet to save this selection. Using interviewer.getAttribute('data-utc').
    let requestObject = {
      interviewer: interviewer.getAttribute('data-email'),
      utc: interviewer.getAttribute('data-utc')
    };
    let requestBody = JSON.stringify(requestObject);
    let request = new Request('/scheduled-interviews', {method: 'POST', body: requestBody});
    fetch(request).then(unused => {}); // TODO: Redirect to scheduled interviews page?
    location.reload();
  }
}

// Fills in the modal with interviewer info from Datastore and shows it.
function showInterviewers(selectButton) {
  const date = selectButton.getAttribute('data-date');
  const time = document.getElementById(date).innerText;
  const reformattedTime = time.replace('-', 'to');
  const utc = document.getElementById(date).value;
  fetch(`/show-interviewers?utc=${utc}&date=${date}&time=${reformattedTime}`)
  .then(response => response.text())
    .then(interviewers => {
      $('#modal-body').html(interviewers);
      $('#modal-title').text(`Interviewers Information for ${date} from ${reformattedTime}`);
    });
  $('#interviewer-modal').modal('show');
}
