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
        var pass = document.getElementById("password").value;

        firebase.auth().signInWithEmailAndPassword(username, pass)
            .then(function() {
                document.getElementById("password").value = null;
                localStorage.setItem("userLogged", username);
                window.location.href = "/backoffice/mainPage.html";
            })
            .catch(function(error) {
                alert(error.message);
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