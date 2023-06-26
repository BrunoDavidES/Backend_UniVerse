function loadLoggedUser() {
    var xmlhttp = new XMLHttpRequest();
    var user = localStorage.getItem("userLogged");

    if(user === ""){
        window.location.href = "/backoffice/index.html";
    }

    xmlhttp.open("GET", document.location.origin + "/rest/profile/" + user, true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xmlhttp.send();

	xmlhttp.onreadystatechange = function() {
	  if (xmlhttp.readyState == 4) {
	    if (xmlhttp.status == 200) {
			var userLogged = JSON.parse(this.responseText);
			document.getElementById("name").innerHTML = userLogged.name;
		}
	  }
    }
}

function logout(){
    var xmlhttp = new XMLHttpRequest();

    xmlhttp.open("POST", document.location.origin + "/rest/logout", true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xmlhttp.send();
    xmlhttp.onreadystatechange = function() {
      if(xmlhttp.readyState == 4) {
        if(xmlhttp.status == 200) {
          localStorage.setItem("userLogged", "");
          window.location.href = "/backoffice/index.html";
        }
        else{
          xmlhttp.responseText;
          window.location.href = "/backoffice/index.html";
        }
      }
    }


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
          popup.classList.remove("open-popup");
          alert(request.responseText);
      } else if (request.readyState === 4) {
          console.log(request.responseText);
          popup.classList.remove("open-popup");
          console.log("FAIL");
      }
    };
}

function editEvent(){
    var id = document.getElementById("idEventMod").value;
    var title = document.getElementById("titleMod").value;
    var startDate = document.getElementById("startDateMod").value;
    var endDate = document.getElementById("endDateMod").value;
    var location = document.getElementById("locationMod").value;
    var department = document.getElementById("departmentMod").value;
    var capacity = document.getElementById("capacityMod").value;
    var isPublic = document.getElementById("isPublicMod").value;
    var isItPaid = document.getElementById("isItPaidMod").value;

  var data = {};

  if (title !== "") {
    data["title"] = title;
  }

  if (startDate !== "") {
      data["startDate"] = startDate;
  }

  if (endDate !== "") {
      data["endDate"] = endDate;
  }

  if (location !== "") {
      data["location"] = location;
  }

  if (department !== "") {
      data["department"] = department;
  }

  if (isPublic !== "") {
      data["isPublic"] = isPublic;
  }

  if (capacity !== "") {
      data["capacity"] = capacity;
  }

  if (isItPaid !== "") {
      data["isItPaid"] = isItPaid;
  }

  var request = new XMLHttpRequest();

  request.open("PATCH", document.location.origin + "/rest/feed/edit/Event/" + id, true);
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
      var id = document.getElementById("idEventDelete").value;

      var request = new XMLHttpRequest();

      request.open("DELETE", document.location.origin + "/rest/feed/delete/Event/" + id, true);
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

function getEvent(){
  var id = document.getElementById("idEventMod").value;

  var idData = {
    "id":id
  };

  var request = new XMLHttpRequest();

  request.open("POST", document.location.origin + "/rest/feed/query/Event?limit=1&offset=0", true);
  request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
  request.send(JSON.stringify(idData));
  request.onreadystatechange = function() {
    if (request.readyState === 4 && request.status === 200) {
    const response = JSON.parse(request.responseText);
                const entities = response.map(function(entity) {
                return {
                    title: entity.properties.title,
                    startDate: entity.properties.startDate,
                    endDate: entity.properties.endDate,
                    location: entity.properties.location,
                    isPublic: entity.properties.isPublic,
                    capacity: entity.properties.capacity,
                    isItPaid: entity.properties.isItPaid,
                    department: entity.properties.department
                    };
                });
        entities.forEach(function(entity) {
            document.getElementById("titleModeLbl").innerHTML = "Título do Evento: " + entity.title.value;
            document.getElementById("startDateModLbl").innerHTML = "Data de Inicio: " + entity.startDate.value;
            document.getElementById("endDateModLbl").innerHTML = "Data de Fim: " + entity.endDate.value;
            document.getElementById("locationModLbl").innerHTML = "Localização: " + entity.location.value;
            document.getElementById("departmentModLbl").innerHTML = "Departamento: " + entity.department.value;
            document.getElementById("capacityLbl").innerHTML = "Capacidade máxima do Evento: " + entity.capacity.value;
            document.getElementById("isPublicLbl").innerHTML = "Evento público: " + entity.isPublic.value;
            document.getElementById("isItPaidLbl").innerHTML = "Evento a pagar: " + entity.isItPaid.value;
        });
    };
  }
}


    //FALTA QUERY!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

var eventsQueryOffset = 0;
var eventsSelect = document.getElementById('listLimitId');


function queryEvents(){
    //const buttonContainer = document.getElementById('button-container');

    var list = document.getElementById('listOfEvents');

    var limit = parseInt(document.getElementById("listLimitId").value);

    var data = {};

/*
    var id = document.getElementById("eventID").value;
    if (id !== "") {
          data["id"] = id;
    }

    var title = document.getElementById("title").value;
    if (title !== "") {
        data["title"] = title;
    }

    var startDate = document.getElementById("startDate").value;
    if (startDate !== "") {
        data["startDate"] = startDate;
    }

    var endDate = document.getElementById("endDate").value;
    if (endDate !== "") {
        data["endDate"] = endDate;
    }
    var location = document.getElementById("location").value;
    if (location !== "") {
        data["location"] = location;
    }
    var department = document.getElementById("department").value;
    if (department !== "") {
        data["department"] = department;
    }
    var isPublic = document.getElementById("isPublic").value;
    if (isPublic !== "") {
        data["isPublic"] = isPublic;
    }
    var capacity = document.getElementById("capacity").value;
    if (capacity !== "") {
        data["capacity"] = capacity;
   }
    var isItPaid = document.getElementById("isItPaid").value;
    if (isItPaid !== "") {
        data["isItPaid"] = isItPaid;
    }
*/
    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/query/Event?limit=" + limit + "&offset=" + eventsQueryOffset, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.send(JSON.stringify(data));
    request.onreadystatechange  = function() {
         if (request.readyState === 4 && request.status === 200) {
            const response = JSON.parse(request.responseText);
            const entities = response.map(function(entity) {
            return {
                authorUsername: entity.properties.authorUsername,
                authorName: entity.properties.authorName,
                title: entity.properties.title,
                startDate: entity.properties.startDate,
                endDate: entity.properties.endDate,
                location: entity.properties.location,
                isPublic: entity.properties.isPublic,
                capacity: entity.properties.capacity,
                isItPaid: entity.properties.isItPaid,
                id: entity.properties.id,
                validated_backoffice: entity.properties.validated_backoffice,
                department: entity.properties.department
                };
            });
        entities.forEach(function(entity) {
            //const button = document.createElement('button');
            //button.textContent = entity.title.value + " " + entity.startDate.value + " - " + entity.endDate.value;
            //buttonContainer.appendChild(button);

            var listItem = document.createElement("li");
            listItem.textContent = entity.title.value + " " + entity.startDate.value + " - " + entity.endDate.value;
            listItem.addEventListener('click', function() {
                  var details = document.getElementById('details');
                  details.innerHTML = '';

                  var title = document.createElement('h2');
                  title.textContent = entity.title.value;
                  details.appendChild(title);

                  var description = document.createElement('p');
                  description.innerHTML = " Nome do evento: " + entity.title.value +
                                            "<br> ID do evento: " + entity.id.value +
                                            "<br> Nome do criador do evento: " + entity.authorName.value +
                                            "<br> Username do criador do evento: " + entity.authorUsername.value +
                                            "<br> Localização: " + entity.location.value +
                                            "<br> Evento público: " + entity.isPublic.value +
                                            "<br> Evento pago: " + entity.isItPaid.value +
                                            "<br> Capacidade: " + entity.capacity.value +
                                            "<br> Início: " + entity.startDate.value +
                                            "<br> Fim: " + entity.endDate.value +
                                            "<br> Departamento organizador: " + entity.department.value +
                                            "<br> Estado de validação pelo Backoffice: " + entity.validated_backoffice.value;

                  details.appendChild(description);

                  var siblings = Array.from(listItem.parentNode.children);
                  var currentIndex = siblings.indexOf(listItem);
                  siblings.slice(currentIndex + 1).forEach(function(sibling) {
                      sibling.classList.toggle('closed');
                  });

                });

            list.appendChild(listItem);

        });
        eventsQueryOffset += limit;
        }
    }
}

function validateEvent(){
  var id = document.getElementById("idEventValidate").value;

  var data = {
    "validated_backoffice": "true"
  };

  var request = new XMLHttpRequest();

  request.open("PATCH", document.location.origin + "/rest/feed/edit/Event/" + id, true);
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

    request.open("PATCH", document.location.origin + "/rest/feed/edit/News/" + id, true);
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

      request.open("DELETE", document.location.origin + "/rest/feed/delete/News/" + id, true);
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

function queryUsers(){

      var limit = document.getElementById("limit").value;
      var offset = document.getElementById("offset").value;

      var data = {};

      var email = document.getElementById("email").value;
      if (email !== "") {
          data.email = email;
      }

      var name = document.getElementById("name").value;
      if (name !== "") {
          data.name = name;
      }

      var role = document.getElementById("role").value;
      if (role !== "") {
          data.role = role;
      }

      var status = document.getElementById("status").value;
      if (status !== "") {
          data.status = status;
      }

      var request = new XMLHttpRequest();

      request.open("POST", document.location.origin + "/rest/modify/profile/query?limit="+limit+"&offset="+offset, true);
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

function getUser(){

      var target = document.getElementById("target").value;

      var request = new XMLHttpRequest();

      request.open("GET", document.location.origin + "/rest/profile/" + target, true);
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

    //REPORTS

function queryReports(){

      var limit = document.getElementById("limit").value;
      var offset = document.getElementById("offset").value;

      var data = {};

      var title = document.getElementById("title").value;
      if (title !== "") {
          data.title = title;
      }
      var id = document.getElementById("id").value;
        if (id !== "") {
            data.id = id;
        }
      var reporter = document.getElementById("reporter").value;
        if (reporter !== "") {
            data.reporter = reporter;
        }
      var location = document.getElementById("location").value;
        if (location !== "") {
            data.location = location;
        }
      var status = document.getElementById("status").value;
        if (status !== "") {
            data.status = status;
        }

      var request = new XMLHttpRequest();

      request.open("POST", document.location.origin + "/rest/modify/reports/query?limit="+limit+"&offset="+offset, true);
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

function reportStatus(){

      var target = document.getElementById("target").value;
      var status = document.getElementById("status").value;

      var request = new XMLHttpRequest();

      request.open("GET", document.location.origin + "/rest/reports/status/" + target + "/" + status, true);
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

window.addEventListener('load', loadLoggedUser);