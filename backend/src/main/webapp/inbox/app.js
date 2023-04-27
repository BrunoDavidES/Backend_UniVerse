function sendMessage() {
    let xhr = new XMLHttpRequest();
    let receiver = document.getElementById('receiver').value;
    let content = document.getElementById('content').value;
    let time = new Date();
    let message = {
        "receiver": receiver,
        "content": content,
        "time_sent": time
    }

    xhr.open('POST', '/rest/inbox/send');
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.onload = function () {
        if (xhr.status === 200) {
            alert('Message sent successfully!');
        } else {
            alert('Failed to send message!');
        }
    };
    xhr.send(JSON.stringify(message));
}

function listReceived() {
    let xhr = new XMLHttpRequest();
    xhr.open('GET', '/rest/inbox/received');
    xhr.send();

    xhr.responseType = "json";
    xhr.onload = function() {
        validateToken();
        if (xhr.status === 200) {
            let response = xhr.response;
            let messageList = document.getElementById("inboxlist");
            messageList.innerHTML = "";
            response.forEach(function(message) {
                let li = document.createElement("li");
                li.textContent = "From: " + message.sender + "\n" + "At: " + message.time_sent + "\n" + message.content + "\n\n";
                messageList.appendChild(li);
            });
        } else {
            alert("FAIL");
        }
    };
    xhr.onerror = function() {
        alert("FAIL");
    };
}

function listSent() {
    let xhr = new XMLHttpRequest();
    xhr.open('GET', '/rest/inbox/sent');
    xhr.send();

    xhr.responseType = "json";
    xhr.onload = function() {
        validateToken();
        if (xhr.status === 200) {
            let response = xhr.response;
            let messageList = document.getElementById("inboxlist");
            messageList.innerHTML = "";
            response.forEach(function(message) {
                let li = document.createElement("li");
                li.textContent = "To: " + message.receiver + "\n" + "At: " + message.time_sent + "\n" + message.content + "\n\n";
                messageList.appendChild(li);
            });
        } else {
            alert("FAIL");
        }
    };
    xhr.onerror = function() {
        alert("FAIL");
    };
}

function logout() {
    document.getElementById("logoutBTN").addEventListener("click", function() {
        let xhr = new XMLHttpRequest();
        xhr.open("POST", "/rest/logout");
        xhr.send();

        xhr.onload = function() {
            if (xhr.status === 200) {
                alert("Logged out successfully.");
                window.location.href = "/login";
            } else {
                alert("FAIL");
            }
        };
        xhr.onerror = function() {
            alert("FAIL");
        };
    });
}

function validateToken() {
    let xhr = new XMLHttpRequest();
    xhr.open("GET", "/rest/token/validate", true);
    xhr.send();
    xhr.onload = function() {
        if (xhr.status === 200) {
            let response = xhr.responseText;
            let values = response.split("<br>");
        } else {
            window.location.href = "/login";
        }
    };
    xhr.onerror = function() {
        window.location.href = "/login";
    };
}

document.addEventListener("DOMContentLoaded", function() {
    validateToken();
    logout();
});
