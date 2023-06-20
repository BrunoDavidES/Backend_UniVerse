 let popup = document.getElementById("popup");

  function openPopup(){
    popup.classList.add("open-popup");
  }

  function closePopup(){
    popup.classList.remove("open-popup");
  }

  let sidebar = document.querySelector(".sidebar");
  let sidebarBtn = document.querySelector(".sidebarBtn");
  sidebarBtn.onclick = function() {
    sidebar.classList.toggle("active");
    if(sidebar.classList.contains("active")){
      sidebarBtn.classList.replace("bx-menu" ,"bx-menu-alt-right");
    }else
      sidebarBtn.classList.replace("bx-menu-alt-right", "bx-menu");
   }

  function loadLoggedUser() {
    var xmlhttp = new XMLHttpRequest();
	var user = localStorage.getItem("userLogged");

	var postLogged = {
		"username": user
	}

	var json = JSON.stringify(postLogged);

	xmlhttp.onreadystatechange = function() {
	  if (xmlhttp.readyState == 4) {
	    if (xmlhttp.status == 200) {
			var userLogged = JSON.parse(this.responseText);

			document.getElementById("name").innerHTML = userLogged.name;

			xmlhttp.open("POST", document.location.origin + "/rest/profile/", true);
			xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
			xmlhttp.send(json);
		}
	  }
	}
  }
	function goToLogin(){
	  if(localStorage.getItem("userLogged") === ""){
	    window.location.href = "/pages/index.html";
      }
	}

function logout(){
    var xmlhttp = new XMLHttpRequest();


    var j = {}

    var json = JSON.stringify(j);
    xmlhttp.onreadystatechange = function() {
      if(xmlhttp.readyState == 4) {
        if(xmlhttp.status == 200) {
          localStorage.setItem("userLogged", "");
          window.location.href = "/pages/index.html";
        }
        else{
          xmlhttp.responseText;
        }
      }
    }

    xmlhttp.open("POST", document.location.origin + "/rest/logout", true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xmlhttp.send(null);
}

  //FEEDS


    //Events
function postEvent(){

    var title = document.getElementById("title").value;
    var startDate = document.getElementById("startDate").value;
    var endDate = document.getElementById("endDate").value;
    var location = document.getElementById("location").value;
    var department = document.getElementById("department").value;
    var isPublic = document.getElementById("isPublic").value;
    var capacity = document.getElementById("capacity").value;
    var isItPaid = document.getElementById("isItPaid").value;

    var data = {
    "title": title,
    "startDate": startDate,
    "endDate": endDate,
    "location": location,
    "department": department,
    "isPublic": isPublic,
    "capacity": capacity,
    "isItPaid": isItPaid
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/post/Event", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.send(JSON.stringify(data));
    request.onreadystatechange  = function() {
      if (request.readyState === 4 && request.status === 200) {
          console.log(request.responseText);
          console.log("SUCCESS");
          alert(request.responseText);
      } else if (request.readyState === 4) {
          console.log(request.responseText);
          console.log("FAIL");
      }
    };
}

function editEvent(){

    var id = document.getElementById("eventID").value;
    var title = document.getElementById("title").value;
    var startDate = document.getElementById("startDate").value;
    var endDate = document.getElementById("endDate").value;
    var location = document.getElementById("location").value;
    var department = document.getElementById("department").value;
    var isPublic = document.getElementById("isPublic").value;
    var capacity = document.getElementById("capacity").value;
    var isItPaid = document.getElementById("isItPaid").value;

  var data = {
  "title": title,
  "startDate": startDate,
  "endDate": endDate,
  "location": location,
  "department": department,
  "isPublic": isPublic,
  "capacity": capacity,
  "isItPaid": isItPaid
  };

  var request = new XMLHttpRequest();

  request.open("PATCH", document.location.origin + "/rest/feed/edit/Event" + id, true);
  request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
  request.send(JSON.stringify(data));
  request.onreadystatechange  = function() {
    if (request.readyState === 4 && request.status === 200) {
        console.log(request.responseText);
        console.log("SUCCESS");
        alert(request.responseText);
    } else if (request.readyState === 4) {
        console.log(request.responseText);
        console.log("FAIL");
    }
  };
}

function deleteEvent(){

      var id = document.getElementById("eventID").value;

      var request = new XMLHttpRequest();

      request.open("DELETE", document.location.origin + "/rest/feed/delete/Event" + id, true);
      request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
      request.send(JSON.stringify(null));
      request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            console.log("SUCCESS");
            alert(request.responseText);
        } else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
        }
      };
    }

    //FALTA QUERY!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!





  //News
function postNews(){

    var title = document.getElementById("title").value;

    var data = {
    "title": title
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/post/News", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.send(JSON.stringify(data));
    request.onreadystatechange  = function() {
    if (request.readyState === 4 && request.status === 200) {
        console.log(request.responseText);
        console.log("SUCCESS");
        alert(request.responseText);
    } else if (request.readyState === 4) {
        console.log(request.responseText);
        console.log("FAIL");
    }
    };
}

function editNews(){

    var id = document.getElementById("newsID").value;
    var title = document.getElementById("title").value;

    var data = {
    "title": title
    };

    var request = new XMLHttpRequest();

    request.open("PATCH", document.location.origin + "/rest/feed/edit/News" + id, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.send(JSON.stringify(data));
    request.onreadystatechange  = function() {
    if (request.readyState === 4 && request.status === 200) {
        console.log(request.responseText);
        console.log("SUCCESS");
        alert(request.responseText);
    } else if (request.readyState === 4) {
        console.log(request.responseText);
        console.log("FAIL");
    }
    };
}

function deleteNews(){

      var id = document.getElementById("newsID").value;

      var request = new XMLHttpRequest();

      request.open("DELETE", document.location.origin + "/rest/feed/delete/News" + id, true);
      request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
      request.send(JSON.stringify(null));
      request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            console.log("SUCCESS");
            alert(request.responseText);
        } else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
        }
      };
    }

        //FALTA QUERY!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


        //USERS
function modifyUserRole(){

      var target = document.getElementById("target").value;
      var newRole = document.getElementById("newRole").value;

      var data = {
            "target": target,
            "newRole": newRole
            };

      var request = new XMLHttpRequest();

      request.open("POST", document.location.origin + "/rest/modify/role", true);
      request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
      request.send(JSON.stringify(data));
      request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            console.log("SUCCESS");
            alert(request.responseText);
        } else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
        }
      };
    }

function queryUsers(){

      var target = document.getElementById("target").value;
      var newRole = document.getElementById("newRole").value;

      var data = {
            "target": target,
            "newRole": newRole
            };

      var request = new XMLHttpRequest();

      request.open("POST", document.location.origin + "/rest/modify/role", true);
      request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
      request.send(JSON.stringify(data));
      request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            console.log("SUCCESS");
            alert(request.responseText);
        } else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
        }
      };
    }


function deleteUser(){

      var target = document.getElementById("target").value;

      var data = {
            "target": target
            };

      var request = new XMLHttpRequest();

      request.open("DELETE", document.location.origin + "/rest/modify/delete", true);
      request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
      request.send(JSON.stringify(data));
      request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            console.log("SUCCESS");
            alert(request.responseText);
        } else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
        }
      };
    }
