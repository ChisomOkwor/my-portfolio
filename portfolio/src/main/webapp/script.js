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


window.onload = function () {
    // document.getElementsById('all').checked=true;
    showAllColumns('all');
}

function showAllColumns(column) {
  var arrayOfAllColumns, i;
  arrayOfAllColumns = document.getElementsByClassName("column");
  if (column == "all") column = "";
  // Add the "show" class (display:block) to the filtered elements, and remove the "show" class from the elements that are not selected
  n = arrayOfAllColumns.length;
  for (i = 0; i < n; i++) {
    w3RemoveClass(arrayOfAllColumns[i], "show");
    if (arrayOfAllColumns[i].className.indexOf(column) > -1) w3AddClass(arrayOfAllColumns[i], "show");
  }
}

// Show filtered elements
function w3AddClass(element, name) {
  var i, arrOfFilteredColumns, showElementArray;
  arrOfFilteredColumns = element.className.split(" ");
  showElementArray = name.split(" ");
  for (i = 0; i < showElementArray.length; i++) {
    if (arrOfFilteredColumns.indexOf(showElementArray[i]) == -1) {
      element.className += " " + showElementArray[i];
    }
  }
}

// Hide elements that are not selected
function w3RemoveClass(element, name) {
  var i,  arrOfFilteredColumns , showElementArray;
  arrOfFilteredColumns = element.className.split(" ");
  showElementArray = name.split(" ");
  for (i = 0; i < showElementArray.length; i++) {
    while ( arrOfFilteredColumns.indexOf(showElementArray[i]) > -1) {
       arrOfFilteredColumns .splice( arrOfFilteredColumns .indexOf(showElementArray[i]), 1);
    }
  }
  element.className =  arrOfFilteredColumns.join(" ");
}

// Add active class to the current button (highlight it)
var btnContainer = document.getElementById("myBtnContainer");
var btns = document.getElementsByClassName("btn");
for (var i = 0; i < btns.length; i++) {
  btns[i].addEventListener("click", function(){
    var current = document.getElementsByClassName("active");
    current[0].className = current[0].className.replace(" active", "");
    this.className += " active";
  });
}


function getServerComments(){
    fetch('/data')  // sends a request to /my-data-url
.then(response => response.json()) // parses the response as JSON
.then((comments) => { // now we can reference the fields in myObject!
  
    const showComments = document.getElementById('comments-container');
    showComments.innerHTML = 'Name: ' + comments.name + ', Email: ' + comments.email + ', Comment: ' + comments.comment;
});
}

