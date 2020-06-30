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

/**
* An AvailabilityTimeSlot represents a table entry on the availability page
* that has a date and time (utcEncoding), a visual represenation of the time
* (timeString), and a class that is set based on whether or not the slot has
* already been selected in a previous visit to the page (green = selected =
* 'table-success').
*/
public class AvailabilityTimeSlot {
  private String utcEncoding;
  private String time;
  private String date;
  private String class;
  
  /**
  * This constructor creates a new Availability object
  */
  public AvailabilityTimeSlot(String utcEncoding; String class) {
    this.utcEncoding = utcEncoding;
    this.time = setTime();
    this.date = setDate();
    this.class = class;
  }
  
  public String getUTCEncoding() {
    return this.utcEncoding;
  }
  
  private String setTime() {
    
  }
  
  private String setDate() {
    
  }
  
  public String getTime() {
    return this.time;
  }
  
  public String getDate() {
    return this.date;
  }
  
  public String getClass() {
    return this.class;
  }
}
