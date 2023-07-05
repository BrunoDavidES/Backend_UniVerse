function verifyLogin() {
  firebase.auth().onAuthStateChanged(function(user) {
    if (!user) {
      window.location.href = "/backoffice/index.html";
    }
  });
}

function loadLoggedUser() {
  firebase.auth().onAuthStateChanged(function(user) {
    if (user) {
      var userId = user.uid;
      console.log("USERID: " + userId);
      var userRef = firebase.database().ref('users/' + userId);
      console.log("USERREF: " + userRef);

      userRef.once('value').then(function(snapshot) {
        var userLogged = snapshot.val();
        console.log("USERLOGGED: " + userLogged);

        //Quando se faz import do database no html, diz q não temos permissão para aceder ao userLogged
        document.getElementById("name").innerHTML = userLogged.name;
        document.getElementById("usernameMail").innerHTML = userLogged.email;
        document.getElementById("role").innerHTML = userLogged.role;
        document.getElementById("departmentTitle").innerHTML = userLogged.department;
        document.getElementById("departmentJobTitle").innerHTML = userLogged.department_job;
      }).catch(function(error) {
        console.error(error);
        window.location.href = "/backoffice/index.html";
      });
    } else {
      window.location.href = "/backoffice/index.html";
    }
  });
}

function logout() {
  firebase.auth().signOut().then(function() {
    sessionStorage.removeItem("userLogged");
    sessionStorage.removeItem("capiToken");
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
    var file = document.getElementById("eventPic").files[0];
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
     var file = document.getElementById("eventPicMod").files[0];
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

  eventPicRef
    .delete()
    .then(function() {
      console.log("Event picture deleted successfully!");
    })
    .catch(function(error) {
      console.error("Error deleting event picture:", error);
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
          console.log(request.responseText);
          uploadEventPic(request.responseText);
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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.send(JSON.stringify(data));
    request.onreadystatechange  = function() {
        if (request.readyState === 4 && request.status === 200) {
            console.log(request.responseText);
            updateEventPicMod(id);
            console.log("SUCCESS");
        }
        else if (request.readyState === 4) {
            console.log(request.responseText);
            console.log("FAIL");
            alert(request.responseText);
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
      console.log(request.responseText);
      deleteEventPic(id); // Call the new function to delete the associated image
      console.log("SUCCESS");
    } else if (request.readyState === 4) {
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

    request.open("POST", document.location.origin + "/rest/feed/query/Event?limit=1&offset=EMPTY", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

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

var queryEventsCursor = "EMPTY";
function queryEvents(){
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
            queryEventsCursor = request.getResponseHeader("X-Cursor");
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


function uploadNewsPic(filename) {
    var file = document.getElementById("newsPic").files[0];
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
     var file = document.getElementById("newsPicMod").files[0];
     if (file != null){
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
                        })
                        .catch(function(error) {
                          console.error("Error putting text body in storage:", error);
                        });

/*                var bucketRequest = new XMLHttpRequest();

                bucketRequest.open("POST", "/gcs/universe-fct.appspot.com/News-" + id + ".txt", true );
                bucketRequest.setRequestHeader("Content-Type", "text/plain");

                bucketRequest.onreadystatechange  = function() {
                    if (bucketRequest.readyState === 4 ) {
                        if (bucketRequest.status === 200 ){
                            alert(request.responseText);

                            console.log("SUCCESS");
                        }
                        else  {
                            console.log("News entity created but error uploading text body to bucket");
                            alert("News entity created but error uploading text body to bucket");
                        }
                    }
                };

                bucketRequest.send(document.getElementById("text").value);*/
            }
            else {
                alert(request.responseText);
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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

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
                if ( text !== localStorage.getItem(id) ){
                    const file = new Blob([text], {type: 'text/plain;charset=UTF-8'});
                    firebase.storage().ref().child("News/" + id + ".txt").put(file)
                        .then(function() {
                              console.log("News text body uploaded successfully!");
                            })
                            .catch(function(error) {
                              console.error("Error putting text body in storage:", error);
                            });
                    updateNewsPicMod(id);

/*                    var bucketPOSTRequest = new XMLHttpRequest();
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
                    bucketPOSTRequest.send(text); */
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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));


    request.onreadystatechange  = function() {
        if ( request.readyState === 4 ) {
            if ( request.status === 200 ) {
                deleteNewsPic(id);



/*                var bucketDELETERequest = new XMLHttpRequest();

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
                bucketDELETERequest.send(""); */
            } else {
                console.log("FAIL");
            }
        };
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
                const entities = response.map(function(entity) {
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
                //var textFileURL = "https://storage.googleapis.com/universe-fct.appspot.com/News/" + id + ".txt";
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
                    localStorage.setItem(id, fileContent);
                    document.getElementById("textMod").value = fileContent;
                  })
                  .catch(function(error) {
                    console.error("Error accessing file:", error);
                  });
/*                fetch(textFileURL)
                    .then(function(response) {
                        if (response.ok) {
                            return response.text();
                        } else {
                            throw new Error("Error fetching text file. Status: " + response.status);
                        }
                    })
                    .then(function(text) {
                        localStorage.setItem(id, text);
                        document.getElementById("textMod").value = text;
                    })
                    .catch(function(error) {
                        console.error("Error downloading text file:", error);
                    });*/
            } else {
                console.log(request.responseText);
                alert("ALGUMA COISA FALHOU");
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
            queryNewsCursor = request.getResponseHeader("X-Cursor");
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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

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

                const entities = response.map(function(entity) {
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
                queryUsersCursor = request.getResponseHeader("X-Cursor");
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
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange = function() {
        if (request.readyState === 4 && request.status === 200) {
           console.log(request.responseText);
           console.log("SUCCESS");
           const response = JSON.parse(request.responseText);

           document.getElementById("usernameInfo").value = response.username;
           document.getElementById("emailInfo").value = response.email;
           document.getElementById("nameInfo").value = response.name;
           document.getElementById("roleInfo").value = response.role;
           document.getElementById("depInfo").value = response.department;  //TRATAR DA LISTA DE JOBS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
           document.getElementById("depInfoJob").value = response.department_job;
           document.getElementById("nucInfo").value = response.nucleus;
           document.getElementById("nucJobInfo").value = response.nucleus_job;
           document.getElementById("officeInfo").value = response.office;
           document.getElementById("licenseeInfo").value = response.license_plate;
           document.getElementById("statusInfo").value = response.status;

        } else if (request.readyState === 4) {
            console.log(request.responseText);
            alert(request.responseText);
            console.log("FAIL");
        }
    };

    request.send();
}

//REPORTS

function getReport(){
var id = document.getElementById("idReport").value;

    var data = {
        "id":id
    };

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/reports/query?limit=1&offset=EMPTY", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

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
                    document.getElementById("creationRep").value = new Date(entity.time_creation.value.seconds * 1000).toString();
                    document.getElementById("lastUpdatedRep").value = new Date(entity.time_lastUpdated.value.seconds * 1000).toString();
                });


                var storageRef = firebase.storage().ref();
                var fileRef = storageRef.child('Reports/' + id + ".txt");
                var imgRef = storageRef.child('Reports/' + id + ".png");

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
                    localStorage.setItem(id, fileContent);
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
                    console.error("Error retrieving image:", error);
                  });


/*                var bucketGETRequest = new XMLHttpRequest();

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
                            alert("Wrong ID for News");
                        }
                    }
                }
                bucketGETRequest.send(); */
            }
            else {
                console.log(request.responseText);
                alert("ALGUMA COISA FALHOU");
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
                    listItem.textContent = entity.title.value + " - " + entity.status.value;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('details');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = " " + entity.title.value;
                        details.appendChild(title);

                        var description = document.createElement('p');
                        description.innerHTML = "&emsp;Título do Report: " + entity.title.value +
                                                "<br> &emsp;ID: " + entity.id.value +
                                                "<br> &emsp;Username do utilizador que fez o report: " + entity.reporter.value +
                                                "<br> &emsp;Localização: " + entity.location.value +
                                                "<br> &emsp;Criado em: " + new Date(entity.time_creation.value.seconds * 1000).toString() +
                                                "<br> &emsp;Última modificação: " + new Date(entity.time_lastUpdated.value.seconds * 1000).toString() +
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
                queryReportsCursor = request.getResponseHeader("X-Cursor");
            }
            else {
                console.log(request.responseText);
                alert("FAIL");
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
                    listItem.textContent = entity.title.value + " - " + entity.status.value;
                    listItem.addEventListener('click', function() {
                        var details = document.getElementById('unresDetails');
                        details.innerHTML = '';

                        var title = document.createElement('h2');
                        title.textContent = " " + entity.title.value;
                        details.appendChild(title);

                        var description = document.createElement('p');
                        description.innerHTML = "&emsp;Título do Report: " + entity.title.value +
                                                "<br> &emsp;ID: " + entity.id.value +
                                                "<br> &emsp;Username do utilizador que fez o report: " + entity.reporter.value +
                                                "<br> &emsp;Localização: " + entity.location.value +
                                                "<br> &emsp;Criado em: " + new Date(entity.time_creation.value.seconds * 1000).toString() +
                                                "<br> &emsp;Última modificação: " + new Date(entity.time_lastUpdated.value.seconds * 1000).toString() +
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
                queryUnresolvedReportsCursor = request.getResponseHeader("X-Cursor");
            }
            else {
                console.log(request.responseText);
                alert("FAIL");
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
                console.log("SUCCESS");
                alert("SUCCESS");
            }
            else {
                console.log(request.responseText);
                console.log("FAIL");
                alert("FAIL");
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

    request.open("POST", document.location.origin + "/rest/department/query/?limit=1&offset=EMPTY", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange = function() {
        if (request.readyState === 4 && request.status === 200) {
           console.log(request.responseText);
           console.log("SUCCESS");
           const response = JSON.parse(request.responseText);

           const entities = response.map(function(entity) {
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
    var location = document.getElementById("locationMod").value;
    var fax = document.getElementById("faxMod").value;

    var data = {
        "id": id,
        "email": email,
        "name": name,
        "president": president,
        "phoneNumber": phoneNumber,
        "location": location,
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
                var bucketGETRequest = new XMLHttpRequest();

                const response = JSON.parse(request.responseText);
                const entities = response.map(function(entity) {
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
                queryDepartmentsCursor = request.getResponseHeader("X-Cursor");
            }
            else {
                console.log(request.responseText);
                alert("FAIL");
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

    if (document.getElementById("description").value !== "") {
                data["description"] = document.getElementById("description").value;
    }

    if (document.getElementById("facebook").value !== "") {
                data["facebook"] = document.getElementById("facebook").value;
    }

    if (document.getElementById("instagram").value !== "") {
                data["instagram"] = document.getElementById("instagram").value;
    }

    if (document.getElementById("linkedin").value !== "") {
                data["linkedIn"] = document.getElementById("linkedin").value;
    }

    var request = new XMLHttpRequest();

    request.open("POST", document.location.origin + "/rest/nucleus/register", true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

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
           console.log("SUCCESS");
           const response = JSON.parse(request.responseText);

           const entities = response.map(function(entity) {
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
            alert(request.responseText);
            console.log("FAIL");
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

function deleteNucleus(){
    var id = document.getElementById("nucIdDel").value;


    var request = new XMLHttpRequest();

    request.open("DELETE", document.location.origin + "/rest/nucleus/delete?id=" + id, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

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

    //request.open("POST", document.location.origin + "/rest/nucleus/query?limit="+limit+"&offset="+queryNucleusCursor, true);
    request.open("POST", document.location.origin + "/rest/nucleus/query?limit="+limit+"&offset="+queryNucleusCursor, true);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.setRequestHeader("Authorization", sessionStorage.getItem("capiToken"));

    request.onreadystatechange  = function() {
        if (request.readyState === 4 ) {
            if (request.status === 200) {
                var bucketGETRequest = new XMLHttpRequest();

                const response = JSON.parse(request.responseText);
                const entities = response.map(function(entity) {
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
                queryNucleusCursor = request.getResponseHeader("X-Cursor");

            }
            else {
                console.log(request.responseText);
                alert("FAIL");
            }
        }
    }
    request.send();
}

function clearList(c1, c2){
    var r1 = document.getElementById(c1);
    var r2 = document.getElementById(c2);
    r1.replaceChildren();
    r2.replaceChildren();
}


window.addEventListener('load', loadLoggedUser);