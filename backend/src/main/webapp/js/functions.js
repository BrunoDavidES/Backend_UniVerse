function verifyLogin() {
  var user = localStorage.getItem("userLogged");
  if(user === ""){
          window.location.href = "/backoffice/index.html";
  }
}

function loadLoggedUser() {
    var xmlhttp = new XMLHttpRequest();
    var user = localStorage.getItem("userLogged");

    if(user === ""){
        window.location.href = "/backoffice/index.html";
    }

    xmlhttp.open("GET", document.location.origin + "/rest/profile/" + user, true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    xmlhttp.onreadystatechange = function() {
      if (xmlhttp.readyState == 4) {
        if (xmlhttp.status == 200) {
            var userLogged = JSON.parse(this.responseText);
            document.getElementById("name").innerHTML = userLogged.name;
            document.getElementById("usernameMail").innerHTML = userLogged.email;
            document.getElementById("role").innerHTML = userLogged.role;
            var str = userLogged.jobs;
            str = str.replace(/^#/,'');
            str = str.replace(/%/g, " - ");
            document.getElementById("jobs").innerHTML = str + '<br>';
        }
        else{
            window.location.href = "/backoffice/index.html";
        }
      }
    }
    xmlhttp.send();
}

function logout(){
    var xmlhttp = new XMLHttpRequest();

    xmlhttp.open("POST", document.location.origin + "/rest/logout", true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

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

    xmlhttp.send();
}

      //FEEDS

//Events
function postEvent(){
    var data = {
        "title": document.getElementById("title").value,
        "startDate": document.getElementById("startDate").value,
        "endDate": document.getElementById("endDate").value,
        "location": document.getElementById("location").value,
        "department": document.getElementById("department").value,
        "isPublic": document.getElementById("isPublic").value,
        "capacity": document.getElementById("capacity").value,
        "isItPaid": document.getElementById("isItPaid").value
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/post/Event", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
      if (request.readyState === 4 && request.status === 200) {
          console.log(request.responseText);
          console.log("SUCCESS");
      } else if (request.readyState === 4) {
          console.log(request.responseText);
          alert(request.responseText);
          console.log("FAIL");
      }
    };

    request.send(JSON.stringify(data));
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
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
            alert(request.responseText);
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
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
            alert(request.responseText);
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

    request.send(JSON.stringify(idData));
}

var eventsQueryOffset = 0;
var eventsSelect = document.getElementById('listLimitId');

function queryEvents(){
    var list = document.getElementById('listOfEvents');
    var limit = parseInt(document.getElementById("listLimitId").value);
    var data = {};

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
                var listItem = document.createElement("li");
                listItem.textContent = entity.title.value + " " + entity.startDate.value + " - " + entity.endDate.value;
                listItem.addEventListener('click', function() {
                      var details = document.getElementById('details');
                      details.innerHTML = '';

                      var title = document.createElement('h2');
                      title.textContent = entity.title.value;
                      details.appendChild(title);

                      var description = document.createElement('p');
                      description.innerHTML = "&emsp;Nome do evento: " + entity.title.value +
                                                "<br> &emsp;ID do evento: " + entity.id.value +
                                                "<br> &emsp;Nome do criador do evento: " + entity.authorName.value +
                                                "<br> &emsp;Username do criador do evento: " + entity.authorUsername.value +
                                                "<br> &emsp;Localização: " + entity.location.value +
                                                "<br> &emsp;Evento público: " + entity.isPublic.value +
                                                "<br> &emsp;Evento pago: " + entity.isItPaid.value +
                                                "<br> &emsp;Capacidade: " + entity.capacity.value +
                                                "<br> &emsp;Início: " + entity.startDate.value +
                                                "<br> &emsp;Fim: " + entity.endDate.value +
                                                "<br> &emsp;Departamento organizador: " + entity.department.value +
                                                "<br> &emsp;Estado de validação pelo Backoffice: " + entity.validated_backoffice.value;

                      details.appendChild(description);

                      var siblings = Array.from(listItem.parentNode.children);
                      var currentIndex = siblings.indexOf(listItem);
                      siblings.slice(currentIndex + 1).forEach(function(sibling) {
                          sibling.classList.toggle('closed');
                      });

                      bottomFunction();

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
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
            alert(request.responseText);
        }
    };
}

//News
function postNews(){

    var data = {
        "title": document.getElementById("title").value,
        "authorNameByBO": document.getElementById("author").value
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/post/News", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200){
                var id = request.responseText;
                var bucketRequest = new XMLHttpRequest();

                bucketRequest.open("POST", "/gcs/universe-fct.appspot.com/News-" + id + ".txt", true );
                bucketRequest.setRequestHeader("Content-Type", "text/plain");

                bucketRequest.onreadystatechange  = function() {
                    if (bucketRequest.readyState === 4 ) {
                        if (bucketRequest.status === 200 ){
                            console.log("SUCCESS");
                        }
                        else  {
                            console.log("News entity created but error uploading text body to bucket");
                            alert("News entity created but error uploading text body to bucket");
                        }
                    }
                };

                bucketRequest.send(document.getElementById("text").value);
            }
            else {
                console.alert(request.responseText);
                console.log("FAIL");
            }
        }
    };
    request.send(JSON.stringify(data));
}

function validateNews(){
    var id = document.getElementById("idNewsValidate").value;

    var data = {
        "validated_backoffice": "true"
    };

    var request = new XMLHttpRequest();

    request.open("PATCH", document.location.origin + "/rest/feed/edit/News/" + id, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.send(JSON.stringify(data));
    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            console.log("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
            alert(request.responseText);
        }
    };
}


function editNews(){

    var id = document.getElementById("idEventMod").value;
    var title = document.getElementById("titleMod").value;
    var authorName = document.getElementById("authorMod").value;
    var text = document.getElementById("textMod").value;

    var data = {};

    if (title !== "") {
        data["title"] = title;
    }

    if (authorName !== ""){
        data["authorNameByBO"] = authorName;
    }

    var request = new XMLHttpRequest();
    request.open("PATCH", document.location.origin + "/rest/feed/edit/News/" + id, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
        if ( request.readyState === 4 ){
            if (request.status === 200) {

                if ( text !== localStorage.getItem(id) ){

                    var bucketPOSTRequest = new XMLHttpRequest();
                    bucketPOSTRequest.open("POST", "/gcs/universe-fct.appspot.com/News-" + id + ".txt");
                    bucketPOSTRequest.setRequestHeader("Content-Type", "text/plain");

                    bucketPOSTRequest.onreadystatechange  = function() {
                        if (bucketPOSTRequest.readyState == 4){
                            if (bucketPOSTRequest.status == 200){
                                console.log("SUCCESS");
                                localStorage.removeItem(id);
                            }
                            else {
                                console.log("News entity edited but error uploading text body to bucket");
                                alert("News entity edited but error uploading text body to bucket");
                            }
                        }
                    }
                    bucketPOSTRequest.send(text);
                }
                else {
                    console.log("SUCCESS");
                }
            }
            else {
                console.log(request.responseText);
                alert("ALGUMA COISA FALHOU");
            }
        }
    };

    request.send(JSON.stringify(data));
}

function deleteNews(){
    var id = document.getElementById("idNewsDelete").value;

    var request = new XMLHttpRequest();

    request.open("DELETE", document.location.origin + "/rest/feed/delete/News/" + id, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
        if ( request.readyState === 4 ) {
            if ( request.status === 200 ) {
                var bucketDELETERequest = new XMLHttpRequest();

                bucketDELETERequest.open("POST", "/gcs/universe-fct.appspot.com/News-" + id + ".txt", true);
                bucketDELETERequest.setRequestHeader("Content-Type", "text/plain");

                bucketDELETERequest.onreadystatechange = function() {
                    if ( bucketDELETERequest.readyState === 4 ) {
                        if ( bucketDELETERequest.status === 200 ) {
                            console.log("SUCCESS");
                        }
                        else{
                            console.log("Problems erasing News txt file content from bucket");
                            alert("Problems erasing News txt file content from bucket");
                        }
                    }
                }
                bucketDELETERequest.send("");
            } else {
                console.log("FAIL");
            }
        };
    }
    request.send();
}


function getNews(){
    var id = document.getElementById("idEventMod").value;

    var data = {
        "id":id
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/query/News?limit=1&offset=0", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange = function() {
        if ( request.readyState === 4 ) {
            if ( request.status === 200 ) {
                const response = JSON.parse(request.responseText);
                const entities = response.map( function(entity) {
                    return {
                        title: entity.properties.title,
                        authorName: entity.properties.authorName
                    }
                });

                entities.forEach(function(entity) {
                    document.getElementById("titleModLbl").innerHTML = "Título da Notícia: " + entity.title.value;
                    document.getElementById("authorModLbl").innerHTML = "Autor da Notícia: " + entity.authorName.value;
                });

                var bucketGETRequest = new XMLHttpRequest();

                bucketGETRequest.open("GET", "/gcs/universe-fct.appspot.com/News-" + id + ".txt");
                bucketGETRequest.setRequestHeader("Content-Type", "text/plain");

                bucketGETRequest.onreadystatechange = function() {
                    if ( bucketGETRequest.readyState === 4 ) {
                        if ( bucketGETRequest.status === 200 ) {
                            localStorage.setItem( id , bucketGETRequest.responseText );
                            document.getElementById("textMod").value = bucketGETRequest.responseText;
                        }
                        else {
                            console.log(request.responseText);
                            alert.log("Wrong ID for News");
                        }
                    }
                }
                bucketGETRequest.send();
            }
            else {
                console.log(request.responseText);
                alert.log("ALGUMA COISA FALHOU");
            }
        }
    }
    request.send(JSON.stringify(data));
}


var newsQueryOffset = 0;

function queryNews(){
    var list = document.getElementById('listOfNews');
    var limit = parseInt(document.getElementById("listLimitId").value);
    var data = {};

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/query/News?limit=" + limit + "&offset=" + newsQueryOffset, true);
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
                id: entity.properties.id,
                validated_backoffice: entity.properties.validated_backoffice,
                };
            });

            entities.forEach(function(entity) {
                var listItem = document.createElement("li");
                listItem.textContent = entity.title.value + " - " + entity.authorName.value;
                listItem.addEventListener('click', function() {
                      var details = document.getElementById('details');
                      details.innerHTML = '';

                      var title = document.createElement('h2');
                      title.textContent = entity.title.value;
                      details.appendChild(title);

                      var description = document.createElement('p');
                      description.innerHTML = " &emsp;Título da notícia: " + entity.title.value +
                                                "<br> &emsp;ID da notícia: " + entity.id.value +
                                                "<br> &emsp;Nome do criador da notícia: " + entity.authorName.value +
                                                "<br> &emsp;Username do utilizador que postou a notícia: " + entity.authorUsername.value +
                                                "<br> &emsp;Estado de validação pelo Backoffice: " + entity.validated_backoffice.value;

                      details.appendChild(description);

                      var siblings = Array.from(listItem.parentNode.children);
                      var currentIndex = siblings.indexOf(listItem);
                      siblings.slice(currentIndex + 1).forEach(function(sibling) {
                          sibling.classList.toggle('closed');
                      });

                      bottomFunction();
                });
                list.appendChild(listItem);

            });
            newsQueryOffset += limit;
        }
    }
}

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

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            console.log("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
            alert(request.responseText);
        }
    };
    request.send(JSON.stringify(data));
}

function deleteUser(){
    var target = document.getElementById("target").value;

    var data = {
        "target": target
    };

    var request = new XMLHttpRequest();

    request.open("DELETE", document.location.origin + "/rest/modify/delete", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            console.log("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
            alert(request.responseText);
        }
    };

    request.send(JSON.stringify(data));
}

var usersQueryOffset = 0;

function queryUsers(){
    var list = document.getElementById("listOfUsers");
    var limit = document.getElementById("listLimitId").value;

    var data = {};

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/profile/query?limit=" + limit + "&offset=" + usersQueryOffset, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if ( request.status === 200 ) {
                const entities = JSON.parse(request.responseText);

                entities.forEach(function(entity) {
                    var listItem = document.createElement("li");
                    listItem.textContent = entity.username + " - " + entity.name;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('details');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = entity.name;
                        details.appendChild(title);

                        var description = document.createElement('p');
                        var jobs = entity.jobs;
                        jobs = jobs.replace();
                        jobs = jobs.replace(/^#/,'');
                        jobs = jobs.replace(/%/g, " - ");
                        description.innerHTML = "&emsp;Nome do user: " + entity.name +
                                                "<br> &emsp;Username: " + entity.username +
                                                "<br> &emsp;Email: " + entity.email +
                                                "<br> &emsp;Role: " + entity.role +
                                                "<br> &emsp;Lista de cargos na faculdade: " + jobs;

                        details.appendChild(description);

                        var siblings = Array.from(listItem.parentNode.children);
                        var currentIndex = siblings.indexOf(listItem);
                        siblings.slice(currentIndex + 1).forEach(function(sibling) {
                            sibling.classList.toggle('closed');
                        });

                        bottomFunction();
                    });
                    list.appendChild(listItem);
                });
                usersQueryOffset += limit;
            }
            else {
                alert("FAIL");
                console.log("FAIL");
            }
        }
    }
    request.send(JSON.stringify(data));
}

function getUser() {
    var target = document.getElementById("targetGetProfile").value;

    var request = new XMLHttpRequest();

    request.open("GET", document.location.origin + "/rest/profile/" + target, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange = function() {
        if (request.readyState === 4 && request.status === 200) {
           console.log(request.responseText);
           console.log("SUCCESS");
           const response = JSON.parse(request.responseText);

           document.getElementById("usernameInfo").value = response.username;
           document.getElementById("nameInfo").value = response.name;
           document.getElementById("roleInfo").value = response.role;
           document.getElementById("jobsInfo").value = response.jobs;  //TRATAR DA LISTA DE JOBS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
           document.getElementById("emailInfo").value = response.email;

        } else if (request.readyState === 4) {
            console.log(request.responseText);
            alert(request.responseText);
            console.log("FAIL");
        }
    };

    request.send();
}

//REPORTS

function getRequest(){
var id = document.getElementById("idReport").value;

    var data = {
        "id":id
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/reports/query?limit=1&offset=0", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange = function() {
        if ( request.readyState === 4 ) {
            if ( request.status === 200 ) {
                const response = JSON.parse(request.responseText);
                const entities = response.map( function(entity) {
                    return {
                        title: entity.properties.title,
                        reporter: entity.properties.reporter,
                        location: entity.properties.location,
                        time_creation: entity.properties.time_creation,
                        time_lastUpdated: entity.properties.time_lastUpdated,
                        status: entity.properties.status
                    }
                });

                entities.forEach(function(entity) {
                    document.getElementById("repTitle").value = entity.title.value;
                    document.getElementById("location").value = entity.location.value;
                    document.getElementById("authorRep").value = entity.reporter.value;
                    document.getElementById("statusRep").value = entity.status.value;
                    document.getElementById("creationRep").value = entity.time_creation.value;
                    document.getElementById("lastUpdatedRep").value = entity.time_lastUpdated.value;
                });

                var bucketGETRequest = new XMLHttpRequest();

                bucketGETRequest.open("GET", "/gcs/universe-fct.appspot.com/Report-" + id + ".txt");
                bucketGETRequest.setRequestHeader("Content-Type", "text/plain");

                bucketGETRequest.onreadystatechange = function() {
                    if ( bucketGETRequest.readyState === 4 ) {
                        if ( bucketGETRequest.status === 200 ) {
                            localStorage.setItem( id , bucketGETRequest.responseText );
                            document.getElementById("textRep").value = bucketGETRequest.responseText;
                        }
                        else {
                            console.log(request.responseText);
                            alert.log("Wrong ID for News");
                        }
                    }
                }
                bucketGETRequest.send();
            }
            else {
                console.log(request.responseText);
                alert.log("ALGUMA COISA FALHOU");
            }
        }
    }
    request.send(JSON.stringify(data));
}

var reportsQueryOffset = 0;
function queryReports(){
    var limit = document.getElementById("limit").value;

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/modify/reports/query?limit="+limit+"&offset="+reportsQueryOffset, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200) {
                var bucketGETRequest = new XMLHttpRequest();

                const response = JSON.parse(request.responseText);
                const entities = response.map(function(entity) {
                    return {
                        title: entity.properties.title,
                        reporter: entity.properties.reporter,
                        location: entity.properties.location,
                        id: entity.properties.id,
                        time_creation: entity.properties.time_creation,
                        time_lastUpdated: entity.properties.time_lastUpdated,
                        status: entity.properties.status
                    };
                });

                entities.forEach(function(entity) {
                    var listItem = document.createElement("li");
                    listItem.textContent = entity.title.value + " - " + entity.authorName.value;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('details');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = entity.title.value;
                        details.appendChild(title);

                        var description = document.createElement('p');
                        description.innerHTML = "&emsp;Título do Report: " + entity.title.value +
                                                "<br> &emsp;ID: " + entity.id.value +
                                                "<br> &emsp;Username do utilizador que fez o report: " + entity.reporter.value +
                                                "<br> &emsp;Localização: " + entity.location.value +
                                                "<br> &emsp;Criado em: " + entity.time_creation.value +
                                                "<br> &emsp;Última modificação: " + entity.time_lastUpdated +
                                                "<br> &emsp;Estado do Report: " + entity.status.value;

                        details.appendChild(description);

                        var siblings = Array.from(listItem.parentNode.children);
                        var currentIndex = siblings.indexOf(listItem);
                        siblings.slice(currentIndex + 1).forEach(function(sibling) {
                            sibling.classList.toggle('closed');
                        });

                        bottomFunction();
                    });
                    list.appendChild(listItem);
                });
                newsQueryOffset += limit;
            }
            else {
                console.log(request.responseText);
                alert.log("Wrong ID for News");
            }
        }
    }
    request.send();
}

function reportStatus(){
    var target = document.getElementById("target").value;
    var status = document.getElementById("status").value;

    var request = new XMLHttpRequest();

    request.open("GET", document.location.origin + "/rest/reports/status/" + target + "/" + status, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            console.log("SUCCESS");
            alert(request.responseText);
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
        }
    };

    request.send();
}

function bottomFunction() {
    window.scrollTo(0,document.body.scrollHeight);
    bottomPage.style.display = 'none';
    topPage.style.display = '';
}

//Departamentos
function deleteDepartment(){
    var id = document.getElementById("dptIdDel").value;


    var request = new XMLHttpRequest();

    request.open("DELETE", document.location.origin + "/rest/department/delete/" + id, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            console.log("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
            alert(request.responseText);
        }
    };

    request.send();
}

function getDepartment() {
    var id = document.getElementById("dptIdMod").value;

    var data = {
        "id": id
    }

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/department/query/?limit=1&offset=0", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange = function() {
        if (request.readyState === 4 && request.status === 200) {
           console.log(request.responseText);
           console.log("SUCCESS");
           const response = JSON.parse(request.responseText);

           const entities = response.map(function(entity) {
               return {
                   address: entity.properties.address,
                   email: entity.properties.email,
                   fax: entity.properties.fax,
                   name: entity.properties.name,
                   phoneNumber: entity.properties.phone_number,
                   president: entity.properties.president
               };
           });

           entities.forEach(function(entity) {
               document.getElementById("addressMod").value = entity.address.value;
               document.getElementById("emailMod").value = entity.email.value;
               document.getElementById("faxMod").value = entity.fax.value;
               document.getElementById("nameDptMod").value = entity.name.value;
               document.getElementById("phoneNumberMod").value = entity.phoneNumber.value;
               document.getElementById("presDptMod").value = entity.president.value;
           });

        } else if (request.readyState === 4) {
            console.log(request.responseText);
            alert(request.responseText);
            console.log("FAIL");
        }
    };

    request.send(JSON.stringify(data));
}

function postDepartment(){
var data = {
        "id": document.getElementById("idDpt").value,
        "email": document.getElementById("email").value,
        "name": document.getElementById("nameDpt").value,
        "president": document.getElementById("presDpt").value,
         "phoneNumber": document.getElementById("phoneNumber").value,
        "address": document.getElementById("address").value,
        "fax": document.getElementById("fax").value

    };
    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/department/register", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
      if (request.readyState === 4 && request.status === 200) {
          console.log(request.responseText);
          console.log("SUCCESS");
      } else if (request.readyState === 4) {
          console.log(request.responseText);
          alert(request.responseText);
          console.log("FAIL");
      }
    };

    request.send(JSON.stringify(data));
}

function editDepartment(){

    var id = document.getElementById("dptIdMod").value;
    var email = document.getElementById("emailMod").value;
    var name = document.getElementById("nameDptMod").value;
    var president = document.getElementById("presDptMod").value;
    var phoneNumber = document.getElementById("phoneNumberMod").value;
    var address = document.getElementById("addressMod").value;
    var fax = document.getElementById("faxMod").value;

    var data = {
            "id": id,
            "email": email,
            "name": name,
            "president": president,
            "phoneNumber": phoneNumber,
            "address": address,
            "fax": fax

        };

    if (email !== "") {
            data["email"] = email;
    }

    if (name !== "") {
            data["name"] = name;
    }

    if (president !== "") {
            data["president"] = president;
    }

    if (phoneNumber !== "") {
            data["phoneNumber"] = phoneNumber;
    }

    if (address !== "") {
            data["address"] = address;
    }

    if (fax !== "") {
            data["fax"] = fax;
    }

    var request = new XMLHttpRequest();
    request.open("POST", document.location.origin + "/rest/department/modify", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
                  console.log(request.responseText);
                  console.log("SUCCESS");
              } else if (request.readyState === 4) {
                  console.log(request.responseText);
                  alert(request.responseText);
                  console.log("FAIL");
              }
    };

    request.send(JSON.stringify(data));
}

//Nucleos
function deleteNucleus(){
    var id = document.getElementById("nucIdDel").value;


    var request = new XMLHttpRequest();

    request.open("DELETE", document.location.origin + "/rest/nucleus/delete/" + id, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            console.log("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
            alert(request.responseText);
        }
    };

    request.send();
}

window.addEventListener('load', loadLoggedUser);