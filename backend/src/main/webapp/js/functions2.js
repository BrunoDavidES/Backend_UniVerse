
function numOfLoggedUsers(){
    var request = new XMLHttpRequest();

    request.open("GET", document.location.origin + "/rest/profile/loggedinuserscount" , true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4){
            if (request.status === 200) {
                 document.getElementById("usersLoggedIn").innerHTML = request.responseText;
            }
            else {
                console.log(request.responseText);
                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");
            }
        }
    };

    request.send();
}

function registeredUsers(){
    var xmlhttp = new XMLHttpRequest();

    xmlhttp.open("POST", document.location.origin + "/rest/profile/numberOfUsers", true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xmlhttp.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState == 4) {
            if(xmlhttp.status == 200) {
                document.getElementById("registered").innerHTML = xmlhttp.responseText;
            }
            else{
                console.log(xmlhttp.responseText);
            }
        }
    }

    xmlhttp.send();
}

function repToSolve(){
    var xmlhttp = new XMLHttpRequest();

    xmlhttp.open("GET", document.location.origin + "/rest/reports/unresolved", true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xmlhttp.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState == 4) {
            if(xmlhttp.status == 200) {
                document.getElementById("unresolvedRep").innerHTML = xmlhttp.responseText;
            }
            else{
                console.log(xmlhttp.responseText);
            }
        }
    }

    xmlhttp.send();
}

function newsToSolve(){
     var notRes = {
        "validated_backoffice": "false"
     };

    var xmlhttp = new XMLHttpRequest();

    xmlhttp.open("POST", document.location.origin + "/rest/feed/numberOf/News", true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xmlhttp.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState == 4) {
            if(xmlhttp.status == 200) {
                document.getElementById("unresolvedNews").innerHTML = xmlhttp.responseText;
            }
            else{
                console.log(xmlhttp.responseText);
            }
        }
    }

    xmlhttp.send(JSON.stringify(notRes));
}

function eventsToSolve(){
     var notRes = {
        "validated_backoffice": false
     };

    var xmlhttp = new XMLHttpRequest();

    xmlhttp.open("POST", document.location.origin + "/rest/feed/numberOf/Event", true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xmlhttp.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState == 4) {
            if(xmlhttp.status == 200) {
                document.getElementById("unresolvedEvents").innerHTML = xmlhttp.responseText;
            }
            else{
                console.log(xmlhttp.responseText);
            }
        }
    }

    xmlhttp.send(JSON.stringify(notRes));
}

function queryEventsWithDates(date1, date2){
    var list = document.getElementById('listOfEvents');
    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/query/Event/timeGap/" + date1 + "/" + date2, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.send(JSON.stringify());
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
            list.innerHTML = "";
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
        }
    }
}

function queryNewsWithDates(date1, date2){
    var data = {};

    var list = document.getElementById("listOfNews");
    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/query/News/timeGap/" + date1 + "/" + date2, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

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
        }
    }
}