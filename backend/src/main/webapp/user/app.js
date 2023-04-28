function listUsernames() {
    let xhr = new XMLHttpRequest();
    xhr.open("GET", "/rest/list");
    xhr.send();

    xhr.responseType = "json";
    xhr.onload = function() {
        validateToken();
        if (xhr.status === 200) {
            let response = xhr.response;
            let usernamesList = document.getElementById("usernames");
            usernamesList.innerHTML = "";
            response.forEach(function(username) {
                let li = document.createElement("li");
                li.textContent = username;
                li.addEventListener("click", function() {
                    selectUser(username);
                });
                usernamesList.appendChild(li);
            });
        } else {
            alert("FAIL");
        }
    };
    xhr.onerror = function() {
        alert("FAIL");
    };
}

function getUserInfo() {
    document.getElementById("submit-button").addEventListener("click", function(event) {
        event.preventDefault();

        let username = document.getElementById("username-input").value;
        let xhr = new XMLHttpRequest();
        xhr.open("GET", "/rest/user/" + username);
        xhr.setRequestHeader("Content-type", "application/json");
        xhr.send();

        xhr.onload = function() {
            validateToken();
            if (this.status === 200) {
                let response = JSON.parse(this.responseText);
                let user = response.properties;

                if(user != null) {
                    document.getElementById("username").textContent = username;
                    document.getElementById("name").textContent = user.name.value;
                    document.getElementById("email").textContent = user.email.value;
                    document.getElementById("landline").textContent = user.landline.value;
                    document.getElementById("mobile").textContent = user.mobile.value;
                    document.getElementById("occupation").textContent = user.occupation.value;
                    document.getElementById("workplace").textContent = user.workplace.value;
                    document.getElementById("address").textContent = user.address.value;
                    document.getElementById("complementary").textContent = user.complementary.value;
                    document.getElementById("city").textContent = user.city.value;
                    document.getElementById("postcode").textContent = user.postcode.value;
                    document.getElementById("nif").textContent = user.nif.value;
                    document.getElementById("privacy").textContent = user.privacy.value;
                    document.getElementById("role").textContent = user.role.value;
                    document.getElementById("status").textContent = user.status.value;
                    document.getElementById("role").textContent = user.role.value;
                    document.getElementById("status").textContent = user.status.value;
                    document.getElementById("ctime").textContent = new Date(user.time_creation.value.seconds * 1000);
                    document.getElementById("lutime").textContent = new Date(user.time_lastupdate.value.seconds * 1000);
                    document.getElementById("updateName").value = user.name.value;
                    document.getElementById("updateEmail").value = user.email.value;
                    document.getElementById("updateLandline").value = user.landline.value;
                    document.getElementById("updateMobile").value = user.mobile.value;
                    document.getElementById("updateOccupation").value = user.occupation.value;
                    document.getElementById("updateWorkplace").value = user.workplace.value;
                    document.getElementById("updateAddress").value = user.address.value;
                    document.getElementById("updateComplementary").value = user.complementary.value;
                    document.getElementById("updateCity").value = user.city.value;
                    document.getElementById("updatePostcode").value = user.postcode.value;
                    document.getElementById("updateNif").value = user.nif.value;
                    document.getElementById("updatePrivacy").value = user.privacy.value;
                    document.getElementById("updateRole").value = user.role.value;
                    document.getElementById("updateStatus").value = user.status.value;
                    document.getElementById("updateRole").value = user.role.value;
                    document.getElementById("updateStatus").value = user.status.value;
                } else {
                    document.getElementById("username").textContent = username;
                    document.getElementById("name").textContent = response.name;
                    document.getElementById("email").textContent = response.email;
                }
                downloadPFP();
            } else {
                alert("FAIL");
            }
        };
        xhr.onerror = function() {
            alert("FAIL");
        };
    });
}

function selectUser(username) {
    document.getElementById("username-input").value = username;
    document.getElementById("submit-button").click();
}

function updateAttributes() {
    let updateForm = document.getElementById("updateAForm");
    updateForm.addEventListener("submit", function(event) {
        event.preventDefault();

        let username = document.getElementById("username").textContent;
        let privacy = document.getElementById("updatePrivacy").value;
        let landline = document.getElementById("updateLandline").value;
        let mobile = document.getElementById("updateMobile").value;
        let occupation = document.getElementById("updateOccupation").value;
        let workplace = document.getElementById("updateWorkplace").value;
        let address = document.getElementById("updateAddress").value;
        let complementary = document.getElementById("updateComplementary").value;
        let city = document.getElementById("updateCity").value;
        let postcode = document.getElementById("updatePostcode").value;
        let nif = document.getElementById("updateNif").value;
        let userData = {
            "privacy": privacy,
            "landline": landline,
            "mobile": mobile,
            "occupation": occupation,
            "workplace": workplace,
            "address": address,
            "complementary": complementary,
            "city": city,
            "postcode": postcode,
            "nif": nif,
        };
        let xhr = new XMLHttpRequest();
        xhr.open("PUT", "/rest/update/attributes/" + username);
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        xhr.send(JSON.stringify(userData));

        xhr.onload = function() {
            validateToken();
            if (xhr.status === 200) {
                alert("SUCCESS");
            } else {
                alert("FAIL");
            }
            closeForms();
        };
        xhr.onerror = function() {
            alert("FAIL");
        };
    });
}

function updateCredentials() {
    let updateForm = document.getElementById("updateCForm");
    updateForm.addEventListener("submit", function(event) {
        event.preventDefault();

        let username = document.getElementById("username").textContent;
        let name = document.getElementById("updateName").value;
        let email = document.getElementById("updateEmail").value;
        let userData = {
            "name": name,
            "email": email
        };
        let xhr = new XMLHttpRequest();
        xhr.open("PUT", "/rest/update/credentials/" + username);
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        xhr.send(JSON.stringify(userData));

        xhr.onload = function() {
            validateToken();
            if (xhr.status === 200) {
                alert("SUCCESS");
            } else {
                alert("FAIL");
            }
            closeForms();
        };
        xhr.onerror = function() {
            alert("FAIL");
        };
    });
}

function updateStatus() {
    let updateForm = document.getElementById("updateSForm");
    updateForm.addEventListener("submit", function(event) {
        event.preventDefault();

        let username = document.getElementById("username").textContent;
        let status = document.getElementById("updateStatus").value;
        let userData = {
            "status": status
        };
        let xhr = new XMLHttpRequest();
        xhr.open("PUT", "/rest/update/status/" + username);
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        xhr.send(JSON.stringify(userData));

        xhr.onload = function() {
            validateToken();
            if (xhr.status === 200) {
                alert("SUCCESS");
            } else {
                alert("FAIL");
            }
            closeForms();
        };
        xhr.onerror = function() {
            alert("FAIL");
        };
    });
}

function updateRole() {
    let updateForm = document.getElementById("updateRForm");
    updateForm.addEventListener("submit", function(event) {
        event.preventDefault();

        let username = document.getElementById("username").textContent;
        let role = document.getElementById("updateRole").value;
        let userData = {
            "role": role
        };
        let xhr = new XMLHttpRequest();
        xhr.open("PUT", "/rest/update/role/" + username);
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        xhr.send(JSON.stringify(userData));

        xhr.onload = function() {
            validateToken();
            if (xhr.status === 200) {
                alert("SUCCESS");
            } else {
                alert("FAIL");
            }
            closeForms();
        };
        xhr.onerror = function() {
            alert("FAIL");
        };
    });
}

function removeUser() {
    let removeButton = document.getElementById("removeBTN");
    removeButton.addEventListener("click", function(event) {
        event.preventDefault();

        let username = document.getElementById("username").textContent;
        let xhr = new XMLHttpRequest();
        xhr.open("DELETE", "/rest/remove/" + username);
        xhr.send();

        xhr.onload = function() {
            if (xhr.status === 200) {
                alert("SUCCESS");
            } else {
                alert("FAIL");
            }
        };
        xhr.onerror = function() {
            alert("FAIL");
        };
    });
}

function downloadPFP() {
    document.getElementsByClassName("profile_picture")[0].src = "https://storage.googleapis.com/universe-202223.appspot.com/" + document.getElementById("username").textContent;
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
            let userRole = values[4];
            let username = values[6];
            elementVisibility(username, userRole);
        } else {
            window.location.href = "/login";
        }
    };
    xhr.onerror = function() {
        window.location.href = "/login";
    };
}

function openUpdateAForm() {
    closeForms();
    document.getElementById("updateAFormPopup").style.display = "block";
}
function openUpdateCForm() {
    closeForms();
    document.getElementById("updateCFormPopup").style.display = "block";
}
function openUpdateRForm() {
    closeForms();
    document.getElementById("updateRFormPopup").style.display = "block";
}
function openUpdateSForm() {
    closeForms();
    document.getElementById("updateSFormPopup").style.display = "block";
}
function openUsernamesList() {
    closeForms();

    document.getElementById("usernamesList").style.display = "block";
    listUsernames();
}
function closeForms() {
    document.getElementById("updateAFormPopup").style.display = "none";
    document.getElementById("updateCFormPopup").style.display = "none";
    document.getElementById("updateRFormPopup").style.display = "none";
    document.getElementById("updateSFormPopup").style.display = "none";
    document.getElementById("usernamesList").style.display = "none";
}

function elementVisibility(username, userRole) {
    const targetname = document.getElementById("username").textContent;
    const targetRole = document.getElementById("role").textContent;

    document.getElementById("updateStatusBTN").style.display = "block";
    document.getElementById("updateRoleBTN").style.display = "block";
    document.getElementById("updateCredentialsBTN").style.display = "block";
    document.getElementById("updateAttributesBTN").style.display = "block";
    document.getElementById("removeBTN").style.display = "block";
    document.getElementById("landline").style.display = "block";
    document.getElementById("mobile").style.display = "block";
    document.getElementById("occupation").style.display = "block";
    document.getElementById("workplace").style.display = "block";
    document.getElementById("address").style.display = "block";
    document.getElementById("complementary").style.display = "block";
    document.getElementById("city").style.display = "block";
    document.getElementById("postcode").style.display = "block";
    document.getElementById("nif").style.display = "block";
    document.getElementById("privacy").style.display = "block";
    document.getElementById("role").style.display = "block";
    document.getElementById("status").style.display = "block";

    if(targetRole === "") {
        document.getElementById("landline").style.display = "none";
        document.getElementById("mobile").style.display = "none";
        document.getElementById("occupation").style.display = "none";
        document.getElementById("workplace").style.display = "none";
        document.getElementById("address").style.display = "none";
        document.getElementById("complementary").style.display = "none";
        document.getElementById("city").style.display = "none";
        document.getElementById("postcode").style.display = "none";
        document.getElementById("nif").style.display = "none";
        document.getElementById("privacy").style.display = "none";
        document.getElementById("role").style.display = "none";
        document.getElementById("status").style.display = "none";
    }

    if(targetRole === ""
        || (userRole === "GBO" && targetRole !== "USER")
        || (userRole === "GA" && targetRole !== "GBO" && targetRole !== "USER")
        || (userRole === "GS" && targetRole === "SU"))
    {
        document.getElementById("updateAttributesBTN").style.display = "none";
        document.getElementById("updateStatusBTN").style.display = "none";
    }

    if(username === targetname) {
        document.getElementById("updateCredentialsBTN").style.display = "none";
    }

    if(userRole === "USER" || userRole === "GBO" || userRole === "GA"
        || (userRole === "GS" && targetRole !== "GBO" && targetRole !== "USER"))
    {
        document.getElementById("updateRoleBTN").style.display = "none";
    }
}

document.addEventListener("DOMContentLoaded", function() {
    validateToken();
    getUserInfo();
    updateAttributes();
    updateCredentials();
    updateRole();
    updateStatus();
    removeUser();
    logout();
});