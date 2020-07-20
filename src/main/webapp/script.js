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

function onIndexLoad() {
  checkLogin();
}

function testCal() {
  fetch('/calendar', {
    mode: 'no-cors',
    redirect: 'manual'
  }).then((response) => {
    console.log(response);
    console.log(response.url);
    window.location.href = response.url;
  }).catch((error) => {
    console.log('testCal() ' + error);
  });  
}

function checkLogin() {
  fetch('/login').then(response => response.json()).then(status => {
    if(status.loggedIn){
      restrictedTabs = document.getElementsByClassName('restricted-tab');
      for(let element of restrictedTabs) {
        element.classList.remove('disabled');
      }
      document.getElementById('login-tab').innerText = 'Logout';
    } else {
      document.getElementById('login-message-container').style.display = 'inline';
      document.getElementById('login-message').innerHTML = 'To get started, please <a href="' + status.changeLogInStatusURL + '">login</a>.';
      restrictedTabs = document.getElementsByClassName('restricted-tab');
      for(let element of restrictedTabs) {
        element.classList.add('disabled');
      }
      document.getElementById('login-tab').innerText = 'Login';
    }
    document.getElementById('login-tab').href = status.changeLogInStatusURL;
  });
}
