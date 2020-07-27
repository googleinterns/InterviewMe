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

function onFeedbackLoad() {
  const loginInfo = getLoginInfo();
  loginInfo.then(supplyLogoutLinkOrRedirectHome); 
  loginInfo.then(getUserOrRedirectRegistration);
  getScheduledInterviewId(); 
  getRole(); 
  loadFeedback(); 
}

function loadFeedback() {
  fetch(`/feedback?timeZone=${getBrowserTimeZone()}&userTime=${getCurrentTime()}&interview=${getScheduledInterviewId()}&role=${getRole()}`)
    .then(response => response.text())
    .then(form => {
      document.getElementById('feedBackForm').innerHTML = form;
    }); 
}

function getBrowserTimeZone() {
  return Intl.DateTimeFormat().resolvedOptions().timeZone; 
}

function getCurrentTime() {
  return new Date().toISOString(); 
}

function getScheduledInterviewId() {
  const queryString = window.location.search;
  const urlParams = new URLSearchParams(queryString);
  const interview = urlParams.get('interview')
  console.log(interview);
  return interview; 
}

function getRole() {
  const queryString = window.location.search;
  const urlParams = new URLSearchParams(queryString);
  const role = urlParams.get('role')
  console.log(role);
  return role; 
}