function login() {
    document.querySelector("#loginForm").addEventListener("submit", function(event) {
        event.preventDefault();
        let username = document.getElementById("username").value;
        let password = document.getElementById("password").value;
        let loginData = {
            "username": username,
            "password": password
        };

        let xhr = new XMLHttpRequest();
        xhr.open("POST", "/rest/login");
        xhr.setRequestHeader("Content-Type", "application/json");

        xhr.send(JSON.stringify(loginData));
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status === 200) {
                    alert("SUCCESS");
                    window.location = "/profile";
                } else {
                    alert("FAIL");
                }
            }
        };
    });
}

document.addEventListener("DOMContentLoaded", function() {
    login();
});
