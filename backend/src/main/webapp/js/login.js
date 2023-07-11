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

document.addEventListener("DOMContentLoaded", function() {
  var loginForm = document.getElementById("loginForm");
  loginForm.addEventListener("submit", function(event) {
    event.preventDefault();
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;

    var hashedPassword = CryptoJS.SHA256(password).toString();

    firebase.auth().signInWithEmailAndPassword(username, hashedPassword)
      .then(function() {
        document.getElementById("password").value = null;
        sessionStorage.setItem("userLogged", username.split("@")[0]);

        firebase.auth().currentUser.getIdToken()
          .then(function(token) {
            // Decode the token to access the payload
            var decodedToken = decodeToken(token);
            if (decodedToken && decodedToken.role !== "A" && decodedToken.role !== "BO") {
              window.location.href = "/backoffice/index.html";
              alert("Invalid role");
              //sessionStorage.removeItem("capiToken");
              //sessionStorage.removeItem("userLogged");
              sessionStorage.clear();
              //localStorage.clear();
            } else {
              var request = new XMLHttpRequest();

              request.open("GET", document.location.origin + "/rest/profile/" + sessionStorage.getItem("userLogged"), true);
              request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
              request.setRequestHeader("Authorization", token);
              request.send();

               request.onreadystatechange = function() {
                  if (request.readyState === 4) {
                    if (request.status === 200) {
                      var response = JSON.parse(request.responseText);

                      sessionStorage.setItem("name", response.name);
                      sessionStorage.setItem("displayRole", response.role);
                      sessionStorage.setItem("departmentTitle", response.department);
                      sessionStorage.setItem("departmentJobTitle", response.department_job);

                      window.location.href = "/backoffice/mainPage.html";
                    }
                    else {
                        alert("Erro " + request.status + "\nNão será possível proceder com o login.\nTente novamente mais tarde.");
                        sessionStorage.clear();

                        window.location.href = "/backoffice/index.html";
                    }
                  }
                }
            }
          })
          .catch(function(error) {
            //sessionStorage.removeItem("capiToken");
            //sessionStorage.removeItem("userLogged");
            sessionStorage.clear();
            localStorage.clear();
            console.error(error);
          });

      });

    firebase.auth().onAuthStateChanged(function(user) {
      if (user) {
        user.getIdToken().then(function(token) {
          var existingToken = sessionStorage.getItem("capiToken");
          if (existingToken) {
            sessionStorage.removeItem("capiToken");
          }
          sessionStorage.setItem("capiToken", token);
        }).catch(function(error) {
          console.error(error);
          alert("Login invalido!!!")
        });
      }
    });
  });
});

  const togglePassword = document.querySelector('#togglePassword');
  const password = document.querySelector('#password');

  togglePassword.addEventListener('click', function (e) {
    // toggle the type attribute
    const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
    password.setAttribute('type', type);
    // toggle the eye slash icon
    this.classList.toggle('fa-eye-slash');
  });

  // Decode the token
  function decodeToken(token) {
    // Implement your decoding logic here
    // You can use a JWT library or a custom decoding function
    // Example:
    // var decodedToken = jwt_decode(token);
    // return decodedToken;

    // For simplicity, we'll assume the token contains the role information
    // in the 'role' claim
    try {
      var payload = token.split('.')[1];
      var decodedPayload = JSON.parse(atob(payload));
      return decodedPayload;
    } catch (error) {
      console.error("Failed to decode token:", error);
      return null;
    }

}

/*
document.addEventListener("DOMContentLoaded", function() {
    var loginForm = document.getElementById("loginForm");
    loginForm.addEventListener("submit", function(event) {
        event.preventDefault();
        var user = document.getElementById("username").value;
        var pass = document.getElementById("password").value;

        var postLogin = {
            "username": user,
            "password": pass
        }

        var xhr = new XMLHttpRequest();

        xhr.open("POST", "/rest/login/backOffice", true);
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.withCredentials = true; // Include credentials (cookies) in the request
        xhr.send(JSON.stringify(postLogin));

        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4 && xhr.status === 200) {
                document.getElementById("password").value = null;
                localStorage.setItem("userLogged", user);
                window.location.href = "/backoffice/mainPage.html";
            } else if (xhr.readyState === 4) {
                alert(xhr.responseText);
            }
        };
    });
});

const togglePassword = document.querySelector('#togglePassword');
const password = document.querySelector('#password');

togglePassword.addEventListener('click', function (e) {
    // toggle the type attribute
    const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
    password.setAttribute('type', type);
    // toggle the eye slash icon
    this.classList.toggle('fa-eye-slash');
});
*/
