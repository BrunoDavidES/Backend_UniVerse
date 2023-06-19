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
                window.location.href = "/pages/mainPage.html";
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
