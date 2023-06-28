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
          popup.classList.remove("open-popup");
          alert(request.responseText);
      } else if (request.readyState === 4) {
          console.log(request.responseText);
          popup.classList.remove("open-popup");
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
            alert(request.responseText);
        }
        else if (request.readyState === 4) {
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
        }
        else if (request.readyState === 4) {
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
                document.getElementById("titleModeLbl").innerHTML = "&emsp;Título do Evento: " + entity.title.value;
                document.getElementById("startDateModLbl").innerHTML = "&emsp;Data de Inicio: " + entity.startDate.value;
                document.getElementById("endDateModLbl").innerHTML = "&emsp;Data de Fim: " + entity.endDate.value;
                document.getElementById("locationModLbl").innerHTML = "&emsp;Localização: " + entity.location.value;
                document.getElementById("departmentModLbl").innerHTML = "&emsp;Departamento: " + entity.department.value;
                document.getElementById("capacityLbl").innerHTML = "&emsp;Capacidade máxima do Evento: " + entity.capacity.value;
                document.getElementById("isPublicLbl").innerHTML = "&emsp;Evento público: " + entity.isPublic.value;
                document.getElementById("isItPaidLbl").innerHTML = "&emsp;Evento a pagar: " + entity.isItPaid.value;
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
            alert(request.responseText);
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
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
                            alert("SUCCESS");
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
                console.log(request.responseText);
                console.log("FAIL");
            }
        }
    };
    request.send(JSON.stringify(data));
}

function validateEvent(){
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
            alert(request.responseText);
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
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
                                alert("SUCCESS");
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
                    alert("SUCCESS");
                }
            }
            else {
                console.log(request.responseText);
                alert("DEU ERRO");
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
                            alert("SUCCESS");
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
                    document.getElementById("titleModLbl").innerHTML = "&emsp;Título da Notícia: " + entity.title.value;
                    document.getElementById("authorModLbl").innerHTML = "&emsp;Autor da Notícia: " + entity.authorName.value;
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
                alert.log("FAIL");
            }
        }
    }

    request.send(JSON.stringify(data));
}

//FALTA QUERY!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

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
                      description.innerHTML = " Título da notícia: " + entity.title.value +
                                                "<br> ID da notícia: " + entity.id.value +
                                                "<br> Nome do criador da notícia: " + entity.authorName.value +
                                                "<br> Username do utilizador que postou a notícia: " + entity.authorUsername.value +
                                                "<br> Estado de validação pelo Backoffice: " + entity.validated_backoffice.value;

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
            alert(request.responseText);
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
        }
    };
    request.send(JSON.stringify(data));
}

function deleteUser(){
    var target = document.getElementById("targetDelUser").value;

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
            alert(request.responseText);
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
        }
    };

    request.send(JSON.stringify(data));
}

function queryUsers(){
    var limit = document.getElementById("limit").value;
    var offset = document.getElementById("offset").value;

    var data = {};

    var email = document.getElementById("email").value;
    if (email !== "") {
        data["email"] = email;
    }

    var name = document.getElementById("name").value;
    if (name !== "") {
        data["name"] = name;
    }

    var role = document.getElementById("role").value;
    if (role !== "") {
        data["role"] = role;
    }

    var status = document.getElementById("status").value;
    if (status !== "") {
        data["status"] = status;
    }

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/modify/profile/query?limit="+limit+"&offset="+offset, true);
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
           alert(request.responseText);
           const response = JSON.parse(request.responseText);

           document.getElementById("usernameInfo").value = response.username;
           document.getElementById("nameInfo").value = response.name;
           document.getElementById("roleInfo").value = response.role;
           document.getElementById("jobsInfo").value = response.jobs;  //TRATAR DA LISTA DE JOBS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
           document.getElementById("emailInfo").value = response.email;

        } else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
        }
    };

    request.send();
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

    request.send(JSON.stringify(data));
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

window.addEventListener('load', loadLoggedUser);