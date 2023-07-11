function verifyLogin() {
  firebase.auth().onAuthStateChanged(function(user) {
    if (!user) {
      window.location.href = "/backoffice/index.html";
    }
  });

  var request = new XMLHttpRequest();
  var token = sessionStorage.getItem("capiToken");

  request.open("GET", document.location.origin + "/rest/profile/verifyBOToken", true);
  request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
  request.setRequestHeader("Authorization", token);
  request.send();

  request.onreadystatechange = function() {
      if (request.readyState === 4) {
          if (request.status === 200) {
              var response = request.responseText;

              if (response === "false"){
                  alert("ERRO\nA sua sessão expirou ou não é válida.");
                  sessionStorage.clear();

                  window.location.href = "/backoffice/index.html";
              }
          }
          else {
              console.error(request.responseText);
              sessionStorage.clear();

              window.location.href = "/backoffice/index.html";
          }
      }
  }
}

function loadLoggedUser() {

    document.getElementById("name").innerHTML = sessionStorage.getItem("name");
    document.getElementById("username").innerHTML = sessionStorage.getItem("userLogged");
    document.getElementById("role").innerHTML = sessionStorage.getItem("displayRole");
    document.getElementById("departmentTitle").innerHTML = sessionStorage.getItem("departmentTitle");
    document.getElementById("departmentJobTitle").innerHTML = sessionStorage.getItem("departmentJobTitle");

    /*
    var request = new XMLHttpRequest();
    var token = sessionStorage.getItem("capiToken");

    request.open("GET", document.location.origin + "/rest/profile/verifyBOToken", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", token);
    request.send();

    request.onreadystatechange = function() {
        if (request.readyState === 4) {
            if (request.status === 200) {
                var response = request.responseText;

                if (response === false){
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                    sessionStorage.clear();

                    window.location.href = "/backoffice/index.html";
                }
            }
            else {
                console.error(request.responseText);
                sessionStorage.clear();

                window.location.href = "/backoffice/index.html";
            }
        }
    }*/


    var miniPic = document.getElementById("miniProfilePic");
    var pic = document.getElementById("profilePic");

    if (sessionStorage.getItem("miniProfilePic") !== null){
        pic.src = sessionStorage.getItem("miniProfilePic");
        miniPic.src = sessionStorage.getItem("miniProfilePic");
    }
    else{
        var storageRef = firebase.storage().ref();
        var imgRef = storageRef.child("Users/" + sessionStorage.getItem("userLogged"));

        imgRef.getDownloadURL()
          .then(function(url) {
            pic.src = url;
            miniPic.src = url;
            sessionStorage.setItem("miniProfilePic", url);
          })
          .catch(function(error) {
            console.error("Error retrieving image:", error);
            miniPic.src = "../img/logo.png";
            pic.src = "../img/logo.png";
          });
    }


 /*
  var request = new XMLHttpRequest();
  var token = sessionStorage.getItem("capiToken");

  request.open("GET", document.location.origin + "/rest/profile/" + sessionStorage.getItem("userLogged"), true);
  request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
  request.setRequestHeader("Authorization", token);

  request.onreadystatechange = function() {
    if (request.readyState === 4) {
      if (request.status === 200) {
        var response = JSON.parse(request.responseText);

        document.getElementById("name").innerHTML = response.name;
        document.getElementById("username").innerHTML = response.email.split("@")[0];
        document.getElementById("role").innerHTML = response.role;
        document.getElementById("departmentTitle").innerHTML = response.department;
        document.getElementById("departmentJobTitle").innerHTML = response.department_job;

        var storageRef = firebase.storage().ref();
        var imgRef = storageRef.child("Users/" + sessionStorage.getItem("userLogged"));
        var miniPic = document.getElementById("miniProfilePic");
        var pic = document.getElementById("profilePic");

        if (sessionStorage.getItem("miniProfilePic") !== null){
            pic.src = sessionStorage.getItem("miniProfilePic");
            miniPic.src = sessionStorage.getItem("miniProfilePic");
        }
        else{
            imgRef.getDownloadURL()
              .then(function(url) {
                pic.src = url;
                miniPic.src = url;
                sessionStorage.setItem("miniProfilePic", url);
              })
              .catch(function(error) {
                console.error("Error retrieving image:", error);
                miniPic.src = "../img/logo.png";
                pic.src = "../img/logo.png";
              });
        }
        // Check if the role is not "A" or "BO" and redirect if necessary
        if (response.role !== "A" && response.role !== "BO") {
            alert("User not allowed!");
            sessionStorage.clear();

            window.location.href = "/backoffice/index.html";
        }
      }
      else {
        console.error(request.responseText);
        sessionStorage.clear();

        window.location.href = "/backoffice/index.html";
      }
    }
  };

  request.send(); */
}

function loadUpperRightInfo(){

    document.getElementById("name").innerHTML = sessionStorage.getItem("userLogged");

    var cachePic = sessionStorage.getItem("miniProfilePic");
    var miniPic = document.getElementById("miniProfilePic");
    if (cachePic === null){
        var storageRef = firebase.storage().ref();
        var imgRef = storageRef.child("Users/" + sessionStorage.getItem("userLogged"));

        imgRef.getDownloadURL()
          .then(function(url) {
            miniPic.src = url;
            sessionStorage.setItem("miniProfilePic", url);
          })
          .catch(function(error) {
            console.error("Error retrieving image:", error);
            miniPic.src = "../img/logo.png";
          });
    }
    else{
        miniPic.src = cachePic;
    }



    /*
    var request = new XMLHttpRequest();
    request.open("GET", document.location.origin + "/rest/profile/" + sessionStorage.getItem("userLogged"), true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange = function() {
        if (request.readyState === 4) {
            if (request.status === 200) {
                var response = JSON.parse(request.responseText);

                document.getElementById("name").innerHTML = response.name;

                if (response.role !== "A" && response.role !== "BO") {
                    alert("User not allowed!");
                    sessionStorage.clear();
                    //sessionStorage.removeItem("capiToken");
                    //sessionStorage.removeItem("userLogged");
                    //sessionStorage.removeItem("miniProfilePic");
                    window.location.href = "/backoffice/index.html";
                }
            }
            else {
                console.log(request.responseText);
                sessionStorage.clear();
                //sessionStorage.removeItem("capiToken");
                //sessionStorage.removeItem("userLogged");
                //sessionStorage.removeItem("miniProfilePic");
                window.location.href = "/backoffice/index.html";
            }
        }
    }

    request.send(); */
}

function hideRoleOption(){
    var token = sessionStorage.getItem("capiToken");

    try {
        var payload = token.split('.')[1];
        var decodedPayload = JSON.parse(atob(payload));

        if (decodedPayload.role === "BO"){
            document.getElementById("BO").style.display = "none";
        }

    } catch (error) {
        console.error("Failed to decode token:", error);
        return null;
    }
/*
    var request = new XMLHttpRequest();

    request.open("GET", document.location.origin + "/rest/profile/" + sessionStorage.getItem("userLogged"), true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange = function() {
        if (request.readyState === 4) {
            if (request.status === 200) {
                var response = JSON.parse(request.responseText);

                if (response.role === "BO"){
                    document.getElementById("BO").style.display = "none";
                }
            }
            else{
                alert(response.role);
                sessionStorage.clear();

                window.location.href = "/backoffice/index.html";
            }
        }
    }
    request.send();
    */
}


function logout() {
  firebase.auth().signOut().then(function() {
    sessionStorage.clear();
    window.location.href = "/backoffice/index.html";
  }).catch(function(error) {
    console.error(error);
    window.location.href = "/backoffice/index.html";
  });
}


var firebaseConfig = {
      apiKey: "AIzaSyB8lbZJXbOkWNkgRxvzqwhLtgGQf5GmpS4",
      authDomain: "universe-fct.firebaseapp.com",
      databaseURL: "https://universe-fct-default-rtdb.europe-west1.firebasedatabase.app",
      projectId: "universe-fct",
      storageBucket: "gs://universe-fct.appspot.com",
      messagingSenderId: "493010584500",
      appId: "1:493010584500:web:9c958f30725cd60533a8e1",
      measurementId: "G-XB1FHSZ4M0"
};
        // Initialize Firebase
firebase.initializeApp(firebaseConfig);


function uploadEventPic(filename) {
    var fileList = document.getElementById("eventPic");

    if (fileList.files.length == 0){
        console.log("No picture to upload");
        return;
    }

    var file = fileList.files[0];

    if(file.size > 8500000){
        alert('Ficheiro demasiado pesado (máximo de 8 MB)');
        document.getElementById("eventPic").value = "";
    }
    else{
        var storageRef = firebase.storage().ref();
        var eventPicRef = storageRef.child("Events/" + filename);

        eventPicRef.put(file).then(function(snapshot) {
            console.log("Event picture uploaded successfully!");
        }).catch(function(error) {
            console.error("Error uploading event picture:", error);
        });
    }

}


function updateEventPicMod(filename) {
    var fileList = document.getElementById("eventPicMod");

    if (fileList.files.length == 0){
        console.log("No picture to upload");
        return;
    }

    var file = fileList.files[0];

    if(file.size > 8500000){
        alert('Ficheiro demasiado pesado (máximo de 8 MB)');
        document.getElementById("eventPicMod").value = "";
    }
    else{
        var storageRef = firebase.storage().ref();
        var eventPicRef = storageRef.child("Events/" + filename);

        eventPicRef.put(file).then(function(snapshot) {
            console.log("Event picture uploaded successfully!");
        }).catch(function(error) {
            console.error("Error uploading event picture:", error);
        });
    }
}


function deleteEventPic(filename) {
  var storageRef = firebase.storage().ref();
  var eventPicRef = storageRef.child("Events/" + filename);
  var eventTxt = storageRef.child("Events/" + filename + ".txt");

  eventPicRef
    .delete()
    .then(function() {
      console.log("Event picture deleted successfully!");
    })
    .catch(function(error) {
      console.error("Error deleting event picture:", error);
    });

  eventTxt
    .delete()
    .then(function() {
      console.log("Event text deleted successfully!");
    })
    .catch(function(error) {
      console.error("Error deleting event description text:", error);
    });
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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            var id = request.responseText;
            uploadEventPic(id);

            const file = new Blob([document.getElementById("text").value], {type: 'text/plain;charset=UTF-8'});

            firebase.storage().ref().child("Events/" + id + ".txt").put(file)
                .then(function() {
                    console.log("Event text body uploaded successfully!");
                    alert("SUCCESS");
                })
                .catch(function(error) {
                    alert("Erro ao guardar a descrição.\nEvento criado sem descrição.")
                    console.error("Error putting text body in storage:", error);
                });
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400 || request.status === 403)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVerifique se preencheu os campos todos.\nCaso o tenha feito, volte a tentar dentro de alguns minutos.");

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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.send(JSON.stringify(data));
    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            updateEventPicMod(id);

            const file = new Blob([document.getElementById("textMod").value], {type: 'text/plain;charset=UTF-8'});

            firebase.storage().ref().child("Events/" + id + ".txt").put(file)
                .then(function() {
                    console.log("Event text body uploaded successfully!");
                    alert("SUCCESS");
                })
                .catch(function(error) {
                    alert("Erro ao guardar a descrição.\nEvento atualizado sem descrição.")
                    console.error("Error putting text body in storage:", error);
                });
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400 || request.status === 403)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");
        }
    };
}

function deleteEvent() {
    var id = document.getElementById("idEventDelete").value;

    var request = new XMLHttpRequest();

    request.open("DELETE", document.location.origin + "/rest/feed/delete/Event/" + id, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.send(JSON.stringify(null));
    request.onreadystatechange = function() {
        if (request.readyState === 4 && request.status === 200) {
            deleteEventPic(id);
            alert("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400 || request.status === 403)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVerifique se preencheu os campos todos.\nCaso o tenha feito, volte a tentar dentro de alguns minutos.");
        }
    };
}


function getEvent(){
    var id = document.getElementById("idEventMod").value;

    var idData = {
        "id":id
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/query/Event?limit=1&offset=EMPTY", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange = function() {
        if (request.readyState === 4 && request.status === 200) {
            const response = JSON.parse(request.responseText);
            const entities = response.results.map(function(entity) {
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

            var storageRef = firebase.storage().ref();
            var fileRef = storageRef.child('Events/' + id + ".txt");

            fileRef.getDownloadURL()
              .then(function(url) {
                return fetch(url);
              })
              .then(function(response) {
                if (response.ok) {
                  return response.text();
                } else {
                  throw new Error("Error fetching file. Status: " + response.status);
                }
              })
              .then(function(fileContent) {
                sessionStorage.setItem(id, fileContent);
                document.getElementById("textMod").value = fileContent;
              })
              .catch(function(error) {
                console.error("Error accessing file:", error);
              });

        }
        else if ( request.readyState === 4 ){
            console.log(request.responseText);
            alert("Erro " + request.status + "\n" + request.responseText);
        }
    }

    request.send(JSON.stringify(idData));
}

var queryEventsCursor = "EMPTY";
function queryEvents(){
    if(queryEventsCursor == null){
        queryEventsCursor = "EMPTY";
    }
    var list = document.getElementById('listOfEvents');
    var limit = parseInt(document.getElementById("listLimitId").value);
    var data = {};

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/query/Event?limit=" + limit + "&offset=" + queryEventsCursor, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.send(JSON.stringify(data));
    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            const response = JSON.parse(request.responseText);
            const entities = response.results.map(function(entity) {
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
            if (response.results.length !== 0)
                queryEventsCursor = response.cursor;
        }
        else if (request.readyState === 4){
            console.log(request.responseText);
            alert("Erro" + request.status + "\n" + request.responseText);
        }
    }
}

function clearListEvents(c1, c2){
    clearList(c1,c2);
    queryEventsCursor = "EMPTY";
}

function validateEvent(){
    var id = document.getElementById("idEventValidate").value;

    var data = {
        "validated_backoffice": "true"
    };

    var request = new XMLHttpRequest();

    request.open("PATCH", document.location.origin + "/rest/feed/edit/Event/" + id, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.send(JSON.stringify(data));
    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            alert("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400 || request.status === 403)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");
        }
    };
}

//News

function uploadNewsPic(filename) {
    var fileList = document.getElementById("newsPic");

    if (fileList.files.length == 0){
        console.log("No picture to upload");
        return;
    }

    var file = fileList.files[0];

    if(file.size > 8500000){
        alert('Ficheiro demasiado pesado (máximo de 8 MB)');
        document.getElementById("newsPic").value = "";
    }
    else{
        var storageRef = firebase.storage().ref();
        var eventPicRef = storageRef.child("News/" + filename);

        eventPicRef.put(file).then(function(snapshot) {
            console.log("News picture uploaded successfully!");
        }).catch(function(error) {
            console.error("Error uploading event picture:", error);
        });
    }

}


function updateNewsPicMod(filename) {
    var fileList = document.getElementById("newsPicMod");

    if (fileList.files.length == 0){
        console.log("No picture to upload");
        return;
    }

    var file = fileList.files[0];

    if(file.size > 8500000){
         alert('Ficheiro demasiado pesado (máximo de 8 MB)');
         document.getElementById("newsPicMod").value = "";
    }
    else{
        var storageRef = firebase.storage().ref();
        var eventPicRef = storageRef.child("News/" + filename);

        eventPicRef.put(file).then(function(snapshot) {
            console.log("News picture uploaded successfully!");
        }).catch(function(error) {
            console.error("Error uploading event picture:", error);
        });
    }
}


function deleteNewsPic(filename) {
  var storageRef = firebase.storage().ref();
  var newsPicRef = storageRef.child("News/" + filename);
  var newsTxtRef = storageRef.child("News/" + filename + ".txt");

  newsPicRef
    .delete()
    .then(function() {
      console.log("News picture deleted successfully!");
    })
    .catch(function(error) {
      console.error("Error deleting news picture:", error);
    });

  newsTxtRef
    .delete()
    .then(function() {
      console.log("News text deleted successfully!");
    })
    .catch(function(error) {
      console.error("Error deleting news text:", error);
    });
}



function postNews(){

    var data = {
        "title": document.getElementById("title").value,
        "authorNameByBO": document.getElementById("author").value
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/post/News", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));


    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200){
                var id = request.responseText;
                uploadNewsPic(id);

                const file = new Blob([document.getElementById("text").value], {type: 'text/plain;charset=UTF-8'});
                firebase.storage().ref().child("News/" + id + ".txt").put(file)
                    .then(function() {
                          console.log("News text body uploaded successfully!");
                          alert("SUCCESS");
                        })
                        .catch(function(error) {
                          console.error("Error putting text body in storage:", error);
                          alert("Erro ao gurdar fotografia do Evento.\nEvento criado sem fotografia.")
                        });
            }
            else {
                alert(request.responseText);
                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else if (request.status === 400 || request.status === 403)
                    alert("ERRO " + request.status + "\n" + request.responseText);
                else
                    alert("ERRO " + request.status +"\nVerifique se preencheu os campos todos.\nCaso o tenha feito, volte a tentar dentro de alguns minutos.");

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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.send(JSON.stringify(data));
    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            alert("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400 || request.status === 403)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");
        }
    };
}


function editNews(){

    var id = document.getElementById("idNewsMod").value;
    var title = document.getElementById("titleMod").value;
    var text = document.getElementById("textMod").value;

    var data = {};

    if (title !== "") {
        data["title"] = title;
    }
    alert(title);

    var request = new XMLHttpRequest();
    request.open("PATCH", document.location.origin + "/rest/feed/edit/News/" + id, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));


    request.onreadystatechange  = function() {
        if ( request.readyState === 4 ){
            if (request.status === 200) {
                updateNewsPicMod(id);
                if ( text !== sessionStorage.getItem(id) && text !== null && text.trim() !== "" ){
                    const file = new Blob([text], {type: 'text/plain;charset=UTF-8'});
                    firebase.storage().ref().child("News/" + id + ".txt").put(file)
                        .then(function() {
                              console.log("News text body uploaded successfully!");
                              sessionStorage.removeItem(id);
                            })
                            .catch(function(error) {
                              console.error("Error putting text body in storage:", error);
                            });
                    updateNewsPicMod(id);
                }
                alert("SUCCESS");
            }
            else {
                console.log(request.responseText);
                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else if (request.status === 400 || request.status === 403)
                    alert("ERRO " + request.status + "\n" + request.responseText);
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));


    request.onreadystatechange  = function() {
        if ( request.readyState === 4 ) {
            if ( request.status === 200 ) {
                deleteNewsPic(id);
                sessionStorage.removeItem(id);
                alert("SUCCESS")
            } else {
                console.log(request.responseText);
                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else if (request.status === 400 || request.status === 403)
                    alert("ERRO " + request.status + "\n" + request.responseText);
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");
            }
        }
    }
    request.send();
}

function getNews() {
    var id = document.getElementById("idNewsMod").value;

    var data = {
        "id": id
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/query/News?limit=1&offset=EMPTY", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));


    request.onreadystatechange = function() {
        if (request.readyState === 4) {
            if (request.status === 200) {
                const response = JSON.parse(request.responseText);
                const entities = response.results.map(function(entity) {
                    return {
                        title: entity.properties.title,
                        authorName: entity.properties.authorName
                    };
                });

                entities.forEach(function(entity) {
                    document.getElementById("titleModLbl").innerHTML = "Título da Notícia: " + entity.title.value;
                    document.getElementById("authorModLbl").innerHTML = "Autor da Notícia: " + entity.authorName.value;
                });

                var storageRef = firebase.storage().ref();
                var fileRef = storageRef.child('News/' + id + ".txt");

                fileRef.getDownloadURL()
                  .then(function(url) {
                    return fetch(url);
                  })
                  .then(function(response) {
                    if (response.ok) {
                      return response.text();
                    } else {
                      throw new Error("Error fetching file. Status: " + response.status);
                    }
                  })
                  .then(function(fileContent) {
                    sessionStorage.setItem(id, fileContent);
                    document.getElementById("textMod").value = fileContent;
                  })
                  .catch(function(error) {
                    console.error("Error accessing file:", error);
                  });
            } else {
                console.log(request.responseText);
                alert("Erro" + request.status + "\n" + request.responseText);
            }
        }
    };
    request.send(JSON.stringify(data));
}

var queryNewsCursor = "EMPTY";
function queryNews(){
    if (queryNewsCursor == null)
        queryNewsCursor = "EMPTY";

    var list = document.getElementById('listOfNews');
    var limit = parseInt(document.getElementById("listLimitId").value);
    var data = {};

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/feed/query/News?limit=" + limit + "&offset=" + queryNewsCursor, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.send(JSON.stringify(data));
    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            const response = JSON.parse(request.responseText);
            const entities = response.results.map(function(entity) {
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
            if (response.results.length !== 0)
                queryNewsCursor = response.cursor;
        }
        else if (request.readyState === 4){
            console.log(request.responseText);
            alert("Erro" + request.status + "\n" + request.responseText);
        }
    }
}

function clearListNews(c1, c2){
    clearList(c1,c2);
    queryNewsCursor = "EMPTY";
}

//USERS
function modifyUserRole(){
    var target = document.getElementById("target").value;
    var newRole = document.getElementById("newRole").value;
    var department = document.getElementById("newDepartment").value;
    var department_job = document.getElementById("newJob").value;
    var office = document.getElementById("newOffice").value;
    var data = {
        "target": target,
        "newRole": newRole,
        "department": department,
        "department_job": department_job,
        "office": office
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/modify/role", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            alert("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVerifique se preencheu os campos todos.\nCaso o tenha feito, volte a tentar dentro de alguns minutos.");
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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            alert("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

        }
    };

    request.send(JSON.stringify(data));
}

function clearListUsers(c1, c2){
    clearList(c1,c2);
    queryUsersCursor = "EMPTY";
}

var queryUsersCursor = "EMPTY";
function queryUsers(){
    if (queryUsersCursor == null){
        queryUsersCursor = "EMPTY";
    }
    var list = document.getElementById("listOfUsers");
    var limit = document.getElementById("listLimitId").value;

    var data = {};

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/profile/query?limit=" + limit + "&offset=" + queryUsersCursor, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if ( request.status === 200 ) {
                const response = JSON.parse(request.responseText);

                const entities = response.results.map(function(entity) {
                    return {
                        name: entity.properties.name,
                        username: (entity.properties.email.value).split("@")[0],
                        email: entity.properties.email,
                        role: entity.properties.role,
                        department: entity.properties.department,
                        department_job: entity.properties.department_job,
                        nucleus: entity.properties.nucleus,
                        nucleus_job: entity.properties.nucleus,
                        license_plate: entity.properties.license_plate,
                        office: entity.properties.office,
                        status: entity.properties.status,
                        time_creation: entity.properties.time_creation,
                        time_lastupdate: entity.properties.time_lastupdate
                    };
                });

                entities.forEach(function(entity) {
                    var listItem = document.createElement("li");
                    listItem.textContent = entity.username + " - " + entity.name.value;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('details');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = entity.name.value;
                        details.appendChild(title);

                        var description = document.createElement('p');
                        description.innerHTML = "&emsp;Nome do user: " + entity.name.value +
                                                "<br> &emsp;Username: " + entity.username +
                                                "<br> &emsp;Email: " + entity.email.value +
                                                "<br> &emsp;Departamento: " + entity.department.value +
                                                "<br> &emsp;Função no departamento: " + entity.department_job.value +
                                                "<br> &emsp;Escritório: " + entity.office.value +
                                                "<br> &emsp;Núcleo: " + entity.nucleus.value +
                                                "<br> &emsp;Função no núcleo: " + entity.nucleus_job.value +
                                                "<br> &emsp;Matrícula da viatura pessoal: " + entity.license_plate.value +
                                                "<br> &emsp;Estado da conta: " + entity.status.value +
                                                "<br> &emsp;Conta criada em: " + new Date(entity.time_creation.value.seconds * 1000).toString(); +
                                                "<br> &emsp;Último update feito em: " + new Date(entity.time_lastupdate.value.seconds * 1000).toString();

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
                if (response.results.length !== 0)
                    queryUsersCursor = response.cursor;
            }
            else {
                console.log(request.responseText);

                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else if (request.status === 400)
                    alert("ERRO " + request.status + "\n" + request.responseText);
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");
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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange = function() {
        if (request.readyState === 4 && request.status === 200) {
           console.log(request.responseText);

           const response = JSON.parse(request.responseText);

           document.getElementById("usernameInfo").value = response.username;
           document.getElementById("emailInfo").value = response.email;
           document.getElementById("nameInfo").value = response.name;
           document.getElementById("roleInfo").value = response.role;
           document.getElementById("depInfo").value = response.department;
           document.getElementById("depInfoJob").value = response.department_job;
           document.getElementById("nucInfo").value = response.nucleus;
           document.getElementById("nucJobInfo").value = response.nucleus_job;
           document.getElementById("officeInfo").value = response.office;
           document.getElementById("licenseeInfo").value = response.license_plate;
           document.getElementById("statusInfo").value = response.status;

        }
        else if (request.readyState === 4) {
            console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

        }
    };

    request.send();
}


function banAccount(){
    var username = document.getElementById("suspendUser").value;

    var request = new XMLHttpRequest();
    request.open("POST", document.location.origin + "/rest/" + username + "/ban");
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange = function() {
        if (request.readyState === 4 ) {
            if ( request.status === 200 ) {
                 alert("SUCCESS");
            }
            else{
                console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
        }
    }
    request.send();
}

function reactivateAccount() {
    var username = document.getElementById("reactivateUser").value;

    var request = new XMLHttpRequest();
    request.open("POST", document.location.origin + "/rest/" + username + "/unban");
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange = function() {
        if (request.readyState === 4 ) {
            if ( request.status === 200 ) {
                 alert("SUCCESS");
            }
            else{
                console.log(request.responseText);
                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else if (request.status === 400)
                    alert("ERRO " + request.status + "\n" + request.responseText);
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
        }
    }
    request.send();
}

//REPORTS

function getReport() {
    var id = document.getElementById("idReport").value;

    var data = {
        "id": id
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/reports/query?limit=1&offset=EMPTY", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange = function() {
        if (request.readyState === 4) {
            if (request.status === 200) {
                const response = JSON.parse(request.responseText);
                const entities = response.results.map(function(entity) {
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
                    document.getElementById("creationRep").value = new Date(entity.time_creation.value.seconds * 1000).toString();

                    var lastUpdatedValue = entity.time_lastUpdated && entity.time_lastUpdated.value && entity.time_lastUpdated.value.seconds;
                    document.getElementById("lastUpdatedRep").value = lastUpdatedValue ? new Date(lastUpdatedValue * 1000).toString() : "";
                });

                var storageRef = firebase.storage().ref();
                var fileRef = storageRef.child('Reports/' + id + ".txt");
                var imgRef = storageRef.child('Reports/' + id);

                fileRef.getDownloadURL()
                    .then(function(url) {
                        return fetch(url);
                    })
                    .then(function(response) {
                        if (response.ok) {
                            return response.text();
                        } else {
                            throw new Error("Error fetching file. Status: " + response.status);
                        }
                    })
                    .then(function(fileContent) {
                        document.getElementById("textRep").value = fileContent;
                    })
                    .catch(function(error) {
                        console.error("Error accessing file:", error);
                    });

                imgRef.getDownloadURL()
                    .then(function(url) {
                        var reportImage = document.getElementById("reportImage");
                        reportImage.src = url;
                    })
                    .catch(function(error) {
                        reportImage.src = "";
                        console.error("Error retrieving image:", error);
                    });
            } else {
                console.log(request.responseText);

                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else if (request.status === 400)
                    alert("ERRO " + request.status + "\n" + request.responseText);
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
        }
    }
    request.send(JSON.stringify(data));
}


function clearListReports(c1, c2){
    clearList(c1,c2);
    queryReportsCursor = "EMPTY";
}

var queryReportsCursor = "EMPTY";
function queryReports(){
    if(queryReportsCursor == null){
        queryReportsCursor = "EMPTY";
    }

    var limit = document.getElementById("listLimitId").value;
    var list = document.getElementById("listOfReports");

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/reports/query?limit="+limit+"&offset="+queryReportsCursor, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200) {
                //var bucketGETRequest = new XMLHttpRequest();

                const response = JSON.parse(request.responseText);
                const entities = response.results.map(function(entity) {
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
                    listItem.textContent = entity.title.value + " - " + entity.status.value;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('details');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = " " + entity.title.value;
                        details.appendChild(title);

                        var updateDateString = "";
                        if (entity.time_lastUpdated && entity.time_lastUpdated.value && entity.time_lastUpdated.value.seconds) {
                            var updateDate = entity.time_lastUpdated.value.seconds;
                            updateDateString = new Date(updateDate * 1000).toString();
                        }

                        var description = document.createElement('p');
                        description.innerHTML = "&emsp;Título do Report: " + entity.title.value +
                                                "<br> &emsp;ID: " + entity.id.value +
                                                "<br> &emsp;Username do utilizador que fez o report: " + entity.reporter.value +
                                                "<br> &emsp;Localização: " + entity.location.value +
                                                "<br> &emsp;Criado em: " + new Date(entity.time_creation.value.seconds * 1000).toString() +
                                                "<br> &emsp;Última modificação: " + updateDateString +
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
                if (response.results.length !== 0)
                    queryReportsCursor = response.cursor;
            }
            else {
                console.log(request.responseText);

                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else if (request.status === 400)
                    alert("ERRO " + request.status + "\n" + request.responseText);
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
        }
    }
    request.send();
}

function clearListUnresReports(c1, c2){
    clearList(c1,c2);
    queryUnresolvedReportsCursor = "EMPTY";
}

var queryUnresolvedReportsCursor = "EMPTY";
function queryUnresolvedReports(){
    if (queryUnresolvedReportsCursor == null){
        queryUnresolvedReportsCursor = "EMPTY";
    }

    var limit = document.getElementById("unResListLimitId").value;
    var list = document.getElementById("listOfUnresReports");

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/reports/query/unresolved?limit="+limit+"&offset=" + queryUnresolvedReportsCursor, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200) {
                //var bucketGETRequest = new XMLHttpRequest();

                const response = JSON.parse(request.responseText);
                const entities = response.results.map(function(entity) {
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
                    listItem.textContent = entity.title.value + " - " + entity.status.value;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('unresDetails');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = " " + entity.title.value;
                        details.appendChild(title);

                        var updateDateString = "";
                        if (entity.time_lastUpdated && entity.time_lastUpdated.value && entity.time_lastUpdated.value.seconds) {
                            var updateDate = entity.time_lastUpdated.value.seconds;
                            updateDateString = new Date(updateDate * 1000).toString();
                        }

                        var description = document.createElement('p');
                        description.innerHTML = "&emsp;Título do Report: " + entity.title.value +
                                                "<br> &emsp;ID: " + entity.id.value +
                                                "<br> &emsp;Username do utilizador que fez o report: " + entity.reporter.value +
                                                "<br> &emsp;Localização: " + entity.location.value +
                                                "<br> &emsp;Criado em: " + new Date(entity.time_creation.value.seconds * 1000).toString() +
                                                "<br> &emsp;Última modificação: " + updateDateString +
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
                if (response.results.length !== 0)
                    queryUnresolvedReportsCursor = response.cursor;
            }
            else {
                console.log(request.responseText);

                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else if (request.status === 400)
                    alert("ERRO " + request.status + "\n" + request.responseText);
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
        }
    }
    request.send();
}


function reportStatus(){
    var id = document.getElementById("idReportStatus").value;
    var status = document.getElementById("newStatus").value;

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/reports/status/" + id + "/" + status, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4){
            if (request.status === 200) {
                alert("SUCCESS");
            }
            else {
                console.log(request.responseText);

                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else if (request.status === 400)
                    alert("ERRO " + request.status + "\n" + request.responseText);
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            alert("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

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

    request.open("POST", document.location.origin + "/rest/department/query/?limit=1&offset=EMPTY", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange = function() {
        if (request.readyState === 4 && request.status === 200) {
           const response = JSON.parse(request.responseText);

           const entities = response.results.map(function(entity) {
               return {
                   location: entity.properties.location,
                   email: entity.properties.email,
                   fax: entity.properties.fax,
                   name: entity.properties.name,
                   phoneNumber: entity.properties.phone_number,
                   president: entity.properties.president
               };
           });

           entities.forEach(function(entity) {
               document.getElementById("locationMod").value = entity.location.value;
               document.getElementById("emailMod").value = entity.email.value;
               document.getElementById("faxMod").value = entity.fax.value;
               document.getElementById("nameDptMod").value = entity.name.value;
               document.getElementById("phoneNumberMod").value = entity.phoneNumber.value;
               document.getElementById("presDptMod").value = entity.president.value;
           });

        } else if (request.readyState === 4) {
            console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else
                alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

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
        "location": document.getElementById("locationDpt").value,
        "fax": document.getElementById("fax").value

    };
    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/department/register", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
      if (request.readyState === 4 && request.status === 200) {
          console.log(request.responseText);
          alert("SUCCESS");
      }
      else if (request.readyState === 4) {
          console.log(request.responseText);

          if (request.status === 401)
              alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
          else if (request.status === 400)
              alert("ERRO " + request.status + "\n" + request.responseText);
          else
              alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

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
    var location = document.getElementById("locationMod").value;
    var fax = document.getElementById("faxMod").value;

    var data = {
        "id": id
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

    if (location !== 0) {
            data["location"] = location;
    }

    if (fax !== "") {
            data["fax"] = fax;
    }

    var request = new XMLHttpRequest();
    request.open("POST", document.location.origin + "/rest/department/modify", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
              alert("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

        }
    };

    request.send(JSON.stringify(data));
}

function clearListDepartments(c1, c2){
    clearList(c1,c2);
    queryDepartmentsCursor = "EMPTY";
}

var queryDepartmentsCursor = "EMPTY";
function queryDepartments(){
    if (queryDepartmentsCursor == null){
        queryDepartmentsCursor = "EMPTY";
    }

    var limit = document.getElementById("listLimitId").value;
    var list = document.getElementById("listOfDepartments");

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/department/query?limit="+limit+"&offset="+queryDepartmentsCursor, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200) {
                //var bucketGETRequest = new XMLHttpRequest();

                const response = JSON.parse(request.responseText);
                const entities = response.results.map(function(entity) {
                    return {
                        name: entity.properties.name,
                        id: entity.properties.id,
                        location: entity.properties.location,
                        email: entity.properties.email,
                        fax: entity.properties.fax,
                        president: entity.properties.president,
                        phone_number: entity.properties.phone_number,
                        time_creation: entity.properties.time_creation,
                        time_lastUpdated: entity.properties.time_lastupdate    ,
                    };
                });

                entities.forEach(function(entity) {
                    var listItem = document.createElement("li");
                    listItem.textContent = entity.name.value + " - " + entity.id.value;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('details');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = entity.name.value;
                        details.appendChild(title);

                        var description = document.createElement('p');
                        description.innerHTML = "&emsp;Nome do Departamento: " + entity.name.value +
                                                "<br> &emsp;ID: " + entity.id.value +
                                                "<br> &emsp;Username do presidente: " + entity.president.value +
                                                "<br> &emsp;Location: " + entity.location.value +
                                                "<br> &emsp;Email: " + entity.email.value +
                                                "<br> &emsp;Fax: " + entity.fax.value +
                                                "<br> &emsp;Número de telefone: " + entity.phone_number.value +
                                                "<br> &emsp;Criado em: " + new Date(entity.time_creation.value.seconds * 1000).toString() +
                                                "<br> &emsp;Última modificação: " + new Date(entity.time_lastUpdated.value.seconds * 1000).toString();

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
                if (response.results.length !== 0)
                    queryDepartmentsCursor = response.cursor;
            }
            else {
                console.log(request.responseText);

                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
        }
    }
    request.send();
}

//Nucleos
function postNucleus(){
var data = {
        "id": document.getElementById("idNuc").value,
        "name": document.getElementById("nameNuc").value,
        "nucleusEmail": document.getElementById("email").value,
        "president": document.getElementById("pres").value,
        "location":document.getElementById("location").value
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/nucleus/register", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
          alert("SUCCESS");
        }
        else if (request.readyState === 4) {
          console.log(request.responseText);

        if (request.status === 401)
            alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
        else if (request.status === 400 || request.status === 403)
            alert("ERRO " + request.status + "\n" + request.responseText);
        else
            alert("ERRO " + request.status +"\nVerifique se preencheu os campos todos.\nCaso o tenha feito, volte a tentar dentro de alguns minutos.");

      }
    };

    request.send(JSON.stringify(data));
}

function getNucleus() {
    var id = document.getElementById("nucId").value;

    var data = {
        "id": id
    }

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/nucleus/query/?limit=1&offset=EMPTY", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange = function() {
        if (request.readyState === 4 && request.status === 200) {
           console.log(request.responseText);

           const response = JSON.parse(request.responseText);

           const entities = response.results.map(function(entity) {
               return {
                   name: entity.properties.name,
                   email: entity.properties.email,
                   president: entity.properties.president,
                   location: entity.properties.location,
                   description: entity.properties.description,
                   facebook: entity.properties.facebook,
                   instagram: entity.properties.instagram,
                   linkedin: entity.properties.linkedIn,
                   website: entity.properties.website,
                   twitter: entity.properties.twitter,
                   youtube: entity.properties.youtube
               };
           });

           entities.forEach(function(entity) {
               document.getElementById("nameMod").value = entity.name.value;
               document.getElementById("emailMod").value = entity.email.value;
               document.getElementById("presMod").value = entity.president.value;
               document.getElementById("locationMod").value = entity.location.value;
               document.getElementById("descriptionMod").value = entity.description.value;
               document.getElementById("facebookMod").value = entity.facebook.value;
               document.getElementById("instagramMod").value = entity.instagram.value;
               document.getElementById("linkedinMod").value = entity.linkedin.value;
               document.getElementById("website").value = entity.website.value;
               document.getElementById("twitter").value = entity.twitter.value;
               document.getElementById("youtube").value = entity.youtube.value;
           });

        } else if (request.readyState === 4) {
            console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else
                alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

        }
    };

    request.send(JSON.stringify(data));
}

function editNucleus(){

    var id = document.getElementById("nucId").value;
    var email = document.getElementById("emailMod").value;
    var name = document.getElementById("nameMod").value;
    var president = document.getElementById("presMod").value;
    var location = document.getElementById("locationMod").value;
    var description = document.getElementById("descriptionMod").value;
    var facebook = document.getElementById("facebookMod").value;
    var instagram = document.getElementById("instagramMod").value;
    var linkedin = document.getElementById("linkedinMod").value;
    var website = document.getElementById("website").value;
    var twitter = document.getElementById("twitter").value;
    var youtube = document.getElementById("youtube").value;

    var data = {
            "id": id
        };

    if (email !== "") {
            data["nucleusEmail"] = email;
    }

    if (name !== "") {
            data["name"] = name;
    }

    if (president !== "") {
            data["president"] = president;
    }

    if (location !== "") {
                data["location"] = location;
        }

    if (description !== "") {
            data["description"] = description;
    }

    if (facebook !== "") {
            data["facebook"] = facebook;
    }

    if (instagram !== "") {
            data["instagram"] = instagram;
    }

    if (linkedin !== "") {
            data["linkedIn"] = linkedin;
    }

    if (website !== "") {
                data["website"] = website;
    }

    if (twitter !== "") {
                data["twitter"] = twitter;
    }

    if (youtube !== "") {
                data["youtube"] = youtube;
    }


    var request = new XMLHttpRequest();
    request.open("POST", document.location.origin + "/rest/nucleus/modify", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            alert("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);

            if (request.status === 401)
                alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
            else if (request.status === 400 || request.status === 403)
                alert("ERRO " + request.status + "\n" + request.responseText);
            else
                alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

        }
    };

    request.send(JSON.stringify(data));
}

function deleteNucleus(){
    var id = document.getElementById("nucIdDel").value;


    var request = new XMLHttpRequest();

    request.open("DELETE", document.location.origin + "/rest/nucleus/delete?id=" + id, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            alert("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);

           if (request.status === 401)
               alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
           else if (request.status === 400)
               alert("ERRO " + request.status + "\n" + request.responseText);
           else
               alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

        }
    };

    request.send();
}

function clearListNucleus(c1, c2){
    clearList(c1,c2);
    queryNucleusCursor = "EMPTY";
}

var queryNucleusCursor = "EMPTY";
function queryNucleus(){
    if (queryNucleusCursor == null){
        queryNucleusCursor = "EMPTY";
    }

    var limit = document.getElementById("listLimitId").value;
    var list = document.getElementById("listOfNucleus");

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/nucleus/query?limit="+limit+"&offset="+queryNucleusCursor, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200) {
                //var bucketGETRequest = new XMLHttpRequest();

                const response = JSON.parse(request.responseText);
                const entities = response.results.map(function(entity) {
                    return {
                        name: entity.properties.name,
                        id: entity.properties.id,
                        description: entity.properties.description,
                        email: entity.properties.email,
                        president: entity.properties.president,
                        facebook: entity.properties.facebook,
                        instagram: entity.properties.instagram,
                        twitter: entity.properties.twitter,
                        website: entity.properties.website,
                        youtube: entity.properties.youtube,
                        time_creation: entity.properties.time_creation,
                        time_lastUpdated: entity.properties.time_lastupdate
                    };
                });

                entities.forEach(function(entity) {
                    var listItem = document.createElement("li");
                    listItem.textContent = entity.name.value + " - " + entity.id.value;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('details');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = entity.name.value;
                        details.appendChild(title);

                        var description = document.createElement('p');
                        description.innerHTML = "&emsp;Nome do Núcleo: " + entity.name.value +
                                                "<br> &emsp;ID: " + entity.id.value +
                                                "<br> &emsp;Username do presidente: " + entity.president.value +
                                                "<br> &emsp;Descrição: " + entity.description.value +
                                                "<br> &emsp;Email: " + entity.email.value +
                                                "<br> &emsp;Presidente: " + entity.president.value +
                                                "<br> &emsp;Facebook: " + entity.facebook.value +
                                                "<br> &emsp;Instagram: " + entity.instagram.value +
                                                "<br> &emsp;Twitter: " + entity.twitter.value +
                                                "<br> &emsp;Website: " + entity.website.value +
                                                "<br> &emsp;Youtube: " + entity.youtube.value +
                                                "<br> &emsp;Criado em: " + new Date(entity.time_creation.value.seconds * 1000).toString() +
                                                "<br> &emsp;Última modificação: " + new Date(entity.time_lastUpdated.value.seconds * 1000).toString();

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
                if (response.results.length !== 0)
                    queryNucleusCursor = response.cursor;
            }
            else {
                console.log(request.responseText);

                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
        }
    }
    request.send();
}

function getHojeNaFCT(){
    var storageRef = firebase.storage().ref();
    var fileRef = storageRef.child("hojenafct.txt");

    fileRef.getDownloadURL()
      .then(function(url) {
        return fetch(url);
      })
      .then(function(response) {
        if (response.ok) {
          return response.text();
        } else {
          throw new Error("Error fetching file. Status: " + response.status);
        }
      })
      .then(function(fileContent) {
        document.getElementById("hojeFCT").value = fileContent;
      })
      .catch(function(error) {
        console.error("Error accessing file:", error);
        alert("Erro a aceder ao ficheiro.");
      });
}


function updateHojeNaFCT(){

    const file = new Blob([document.getElementById("hojeFCT").value], {type: 'text/plain;charset=UTF-8'});

    firebase.storage().ref().child("hojenafct.txt").put(file)
        .then(function() {
            console.log("Hoje na FCT updated successfully!");
        })
        .catch(function(error) {
            console.error("Error putting text body in storage:", error);
            alert("Erro a guardar o ficheiro.\nTente novamente dentro de alguns minutos.");
        });
}

function clearList(c1, c2){
    var r1 = document.getElementById(c1);
    var r2 = document.getElementById(c2);
    r1.replaceChildren();
    r2.replaceChildren();
}

function clearListFAQ(c1, c2){
    clearList(c1,c2);
    queryFAQCursor = "EMPTY";
}

var queryFAQCursor = "EMPTY";
function queryFAQ(){
    if(queryFAQCursor == null){
        queryFAQCursor = "EMPTY";
    }

    var limit = document.getElementById("listLimitId").value;
    var list = document.getElementById("listOfFAQS");

    var request = new XMLHttpRequest();

    request.open("GET", document.location.origin + "/rest/help/view?size="+limit+"&cursor="+queryFAQCursor, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200) {
                //var bucketGETRequest = new XMLHttpRequest();

                const response = JSON.parse(request.responseText);
                const entities = response.results.map(function(entity) {
                    return {
                        id: entity.key.path[0],
                        email: entity.properties.email,
                        title: entity.properties.title,
                        message: entity.properties.message,
                        replied: entity.properties.replied,
                        submitted: entity.properties.submitted
                    };
                });

                entities.forEach(function(entity) {
                    var listItem = document.createElement("li");
                    listItem.textContent = entity.title.value + " - " + entity.email.value;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('details');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = " " + entity.title.value;
                        details.appendChild(title);

                        var description = document.createElement('p');
                        description.innerHTML = "&emsp;Email de quem mandou: " + entity.email.value +
                                                "<br> &emsp;ID: " + entity.id.name +
                                                "<br> &emsp;Enviado em: " + new Date(entity.submitted.value.seconds * 1000).toString() +
                                                "<br> &emsp;Repondido: " + (entity.replied.value !== "" ? new Date(entity.replied.value.seconds * 1000).toString() : "" ) +
                                                "<br> &emsp;Pergunta: " + entity.message.value;

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
                if (response.results.length !== 0)
                    queryFAQCursor = response.cursor;
            }
            else {
                console.log(request.responseText);

                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
        }
    }
    request.send();
}

function clearListFAQUnres(c1, c2){
    clearList(c1,c2);
    queryUnresFAQCursor = "EMPTY";
}

var queryUnresFAQCursor = "EMPTY";
function queryUnresFAQ(){
    if(queryUnresFAQCursor == null){
        queryUnresFAQCursor = "EMPTY";
    }

    var limit = document.getElementById("unResListLimitId").value;
    var list = document.getElementById("listOfUnresFAQS");

    var request = new XMLHttpRequest();

    request.open("GET", document.location.origin + "/rest/help/view/unanswered?size="+limit+"&cursor="+queryFAQCursor, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200) {
                //var bucketGETRequest = new XMLHttpRequest();

                const response = JSON.parse(request.responseText);
                const entities = response.results.map(function(entity) {
                    return {
                        id: entity.key.path[0],
                        email: entity.properties.email,
                        title: entity.properties.title,
                        message: entity.properties.message,
                        submitted: entity.properties.submitted
                    };
                });

                entities.forEach(function(entity) {
                    var listItem = document.createElement("li");
                    listItem.textContent = entity.title.value + " - " + entity.email.value;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('unresDetails');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = " " + entity.title.value;
                        details.appendChild(title);

                        var description = document.createElement('p');
                        description.innerHTML = "&emsp;Email de quem mandou: " + entity.email.value +
                                                "<br> &emsp;ID: " + entity.id.name +
                                                "<br> &emsp;Enviado em: " + new Date(entity.submitted.value.seconds * 1000).toString() +
                                                "<br> &emsp;Pergunta: " + entity.message.value;

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
                if (response.results.length !== 0)
                    queryUnresFAQCursor = response.cursor;
            }
            else {
                console.log(request.responseText);

                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
        }
    }
    request.send();
}

function faqAnswered(){
    var id = document.getElementById("idFAQStatus").value;

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/help/" + id + "/answer", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4){
            if (request.status === 200) {
                alert("Pergunta marcada como respondida.");
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

function clearListFeedBack(c1, c2){
    clearList(c1,c2);
    queryFeedBackCursor = "EMPTY";
}

var queryFeedBackCursor = "EMPTY";
function queryFeedBack(){
    if (queryFeedBackCursor === null){
        queryFeedBackCursor = "EMPTY";
    }

    var limit = document.getElementById("listLimitId").value;
    var list = document.getElementById("listOfFeedbacks");

    var request = new XMLHttpRequest();

    request.open("GET", document.location.origin + "/rest/feedback/view?size=" + limit + "&cursor=" + queryFeedBackCursor, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200) {
                const response = JSON.parse(request.responseText);
                const entities = response.results.map(function(entity) {
                    return {
                        id: entity.key.path[0],
                        author: entity.properties.author,
                        message: entity.properties.message,
                        submitted: entity.properties.submitted,
                        rating: entity.properties.rating
                    };
                });

                entities.forEach(function(entity) {
                    var listItem = document.createElement("li");
                    listItem.textContent = entity.author.value;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('feedBackDetails');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = " " + entity.id.name;
                        details.appendChild(title);

                        var description = document.createElement('p');
                        var descriptionTXT = "&emsp;Autor da mensagem de feedback: " + entity.author.value +
                                             "<br> &emsp;ID: " + entity.id.name +
                                             "<br> &emsp;Enviado em: " + new Date(entity.submitted.value.seconds * 1000).toString() +
                                             "<br> &emsp;Classificação: " + entity.rating.value +
                                             "<br> &emsp;Conteúdo do Feedback: ";

                        var storageRef = firebase.storage().ref();
                        var fileRef = storageRef.child('Feedback/' + entity.id.name + ".txt");

                        fileRef.getDownloadURL()
                          .then(function(url) {
                            return fetch(url);
                          })
                          .then(function(response) {
                            if (response.ok) {
                              return response.text();
                            } else {
                              throw new Error("Error fetching file. Status: " + response.status);
                            }
                          })
                          .then(function(fileContent) {
                             description.innerHTML = descriptionTXT + fileContent;
                          })
                          .catch(function(error) {
                            console.error("Error accessing file:", error);
                            description.innerHTML = descriptionTXT;
                          });

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
                if (response.results.length !== 0)
                    queryFeedBackCursor = response.cursor;
            }
            else {
                console.log(request.responseText);

                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else if (request.status === 403)
                    alert("ERRO " + request.status + "\nNão tem permissão para listar FeedBacks.");
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
        }
    }
    request.send();
}

function statsFeedBack(){
    var request = new XMLHttpRequest();

    request.open("GET", document.location.origin + "/rest/feedback/stats", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200) {
                const response = JSON.parse(request.responseText);
                document.getElementById("ratingOvr").innerHTML = response[0];
                document.getElementById("submissions").innerHTML = response[1];
            }
            else {
                console.log(request.responseText);

                if (request.status === 401)
                    alert("ERRO " + request.status +"\nA sua sessão expirou ou não é válida.");
                else
                    alert("ERRO " + request.status +"\nVolte a tentar dentro de alguns minutos.");

            }
        }
    }
    request.send();
}
