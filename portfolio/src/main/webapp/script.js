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
	showAllColumns('hacks');
	loadComments();
	createMap();
	showCommentForm();
	showLogoutBtn();
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
	var i, arrOfFilteredColumns, showElementArray;
	arrOfFilteredColumns = element.className.split(" ");
	showElementArray = name.split(" ");
	for (i = 0; i < showElementArray.length; i++) {
		while (arrOfFilteredColumns.indexOf(showElementArray[i]) > -1) {
			arrOfFilteredColumns.splice(arrOfFilteredColumns.indexOf(showElementArray[i]), 1);
		}
	}
	element.className = arrOfFilteredColumns.join(" ");
}

// Add active class to the current button (highlight it)
var btnContainer = document.getElementById("myBtnContainer");
var btns = document.getElementsByClassName("btn");
for (var i = 0; i < btns.length; i++) {
	btns[i].addEventListener("click", function () {
		var current = document.getElementsByClassName("active");
		current[0].className = current[0].className.replace(" active", "");
		this.className += " active";
	});
}

function validateForm() {
	var isEmptyName = document.forms["commentForm"]["name"].value;
	var isEmptyComment = document.forms["commentForm"]["comment"].value;

	if (isEmptyName == "" || isEmptyEmail == "" || isEmptyComment == "") {
		alert("All fields must be completed!");
		return false;
	}
}


function loadComments() {
	const numValue = document.getElementById('numComments').value;
	fetch('/data?numValue=' + numValue).then(response => response.json()).then((Data) => {
		const userDataListElement = document.getElementById('commentsContainer');
		userDataListElement.textContent = '';
		Data.forEach((userData) => {
			userDataListElement.appendChild(createListElement(userData));
		})
	});
}

function createListElement(userData) {
	const userDataElement = document.createElement('li');
	userDataElement.className = 'userData';

	const commentElement = document.createElement('span');
	commentElement.innerText = userData.comment;
	commentElement.className = 'commentStyle';

	const nameElement = document.createElement('span');
	nameElement.innerText = userData.name;
	nameElement.className = 'nameStyle';

	const dateElement = document.createElement('span');
	dateElement.innerText = userData.date;
	dateElement.className = 'dateStyle';

	const deleteButtonElement = document.createElement('button');
	deleteButtonElement.innerText = 'Delete';
	deleteButtonElement.className = 'deleteBtnStyle'

	deleteButtonElement.addEventListener('click', () => {
		deleteData(userData);

		// Remove the task from the DOM.
		userDataElement.remove();
	});

	var linebreak = document.createElement("br");

	var para = document.createElement('span');
	para.innerHTML = "by:  ";

	var para2 = document.createElement('span');
	para2.innerHTML = "    ";

	userDataElement.appendChild(dateElement);
	userDataElement.appendChild(commentElement);
	userDataElement.appendChild(linebreak);
	userDataElement.appendChild(para);
	userDataElement.appendChild(nameElement);
	userDataElement.appendChild(para2);
	userDataElement.appendChild(deleteButtonElement);

	return userDataElement;
}

/** Tells the server to delete the data. */
function deleteData(data) {
	const params = new URLSearchParams();
	params.append('id', data.id);
	fetch('/delete-data', {
		method: 'POST',
		body: params
	});
}

// Creates a Map with Markers of Chick fil A locations in Tennessee
function createMap() {
	const map = new google.maps.Map(
		document.getElementById('map'), {
			center: {
				lat: 35.832308,
				lng: -86.317786
			},
			zoom: 8
		});

	var iconBase = 'https://maps.google.com/mapfiles/kml/shapes/';

	const fiskUniversityMarker = new google.maps.Marker({
		position: {
			lat: 36.1663542,
			lng: -86.8031514
		},
		map: map,
		title: 'My College Campus',
		icon: iconBase + 'parking_lot_maps.png'
	});

	fetch('/chicFilA-data').then(response => response.json()).then((chicFilACoordinates) => {
		chicFilACoordinates.forEach((chicFilACoordinate) => {
			new google.maps.Marker({
				position: {
					lat: chicFilACoordinate.lat,
					lng: chicFilACoordinate.lng
				},
				map: map
			});
		});
	});
}

function fetchLoginUrl() {
	fetch('/login').then(response => response.json()).then((loginData) => {
		var url = loginData.loginUrl;
		if (loginData.isUserLoggedIn == true) {
			url = loginData.logoutUrl;

		}
		window.location.assign(url);
	});
}

function showLogoutBtn() {
	fetch('/login').then(response => response.json()).then((loginData) => {
		var btn = "LOGIN";
		if (loginData.isUserLoggedIn == true) {
			btn = "LOGOUT";
		}
		document.getElementById("loginBtn").textContent = btn;
	});
}

function showCommentForm() {
	fetch('/login').then(response => response.json()).then((loginData) => {
		var isUserLoggedIn = loginData.isUserLoggedIn;
		document.getElementById("commentForm").style.display = "none";
		document.getElementById("aboutCommentsContainer").innerHTML =
			"<h2 style='color: #24c770'> Login to add Comments </h2>";

		if (isUserLoggedIn == true) {
			document.getElementById("commentForm").style.display = "block";
			document.getElementById("aboutCommentsContainer").innerHTML =
				"<h2 style='color: #24c770'> Comments</h2>";
		}

	});
}