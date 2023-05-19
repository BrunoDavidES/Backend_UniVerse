function register() {
    let registerForm = document.getElementById("registerForm");

    registerForm.addEventListener("submit", function (event) {
        event.preventDefault();
        let password = document.getElementById("password").value;
        let confirmation = document.getElementById("confirmation").value;

        if (password !== confirmation) {
            alert("Passwords do not match");
        } else {
            let username = document.getElementById("username").value;
            let name = document.getElementById("name").value;
            let email = document.getElementById("email").value;
            let userData = {
                "username": username,
                "name": name,
                "email": email,
                "password": password,
                "confirmation": confirmation,
            };
            let xhr = new XMLHttpRequest();

            xhr.open("POST", "/rest/register", true);
            xhr.setRequestHeader("Content-Type", "application/json");

            xhr.send(JSON.stringify(userData));
            xhr.onload = function () {
                if (xhr.status === 200) {
                    alert("SUCCESS");
                    window.location = "..";
                } else {
                    alert("FAIL");
                }
            };
            xhr.onerror = function() {
                alert("FAIL");
            };
        }
    });
}

document.addEventListener("DOMContentLoaded", function() {
    register();
});