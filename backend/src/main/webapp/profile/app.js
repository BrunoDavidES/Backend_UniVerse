function loadUserInfo(username, userRole) {
    let xhr = new XMLHttpRequest();
    xhr.open("GET", "/rest/user/" + username);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.send();

    xhr.onload = function() {
        if (this.status === 200) {
            let response = JSON.parse(this.responseText);
            let user = response.properties;
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

            elementVisibility(username, userRole);
            downloadPFP();
        } else {
            alert("FAIL");
        }
    };
    xhr.onerror = function() {
        alert("FAIL");
    };
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

function updatePassword() {
    let updateForm = document.getElementById("updatePForm");
    updateForm.addEventListener("submit", function(event) {
        event.preventDefault();

        let password = document.getElementById("password").value;
        let confirmation = document.getElementById("confirmation").value;
        let userData = {
            "password": password,
            "confirmation": confirmation
        };
        let xhr = new XMLHttpRequest();
        xhr.open("PUT", "/rest/update/password");
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

function createEntity() {
    let updateForm = document.getElementById("createEForm");
    updateForm.addEventListener("submit", function(event) {
        event.preventDefault();

        let kind = document.getElementById("kind").value;
        let key = document.getElementById("key").value;
        let attribute1 = document.getElementById("attribute1").value;
        let value1 = document.getElementById("value1").value;
        let attribute2 = document.getElementById("attribute2").value;
        let value2 = document.getElementById("value2").value;
        let attribute3 = document.getElementById("attribute3").value;
        let value3 = document.getElementById("value3").value;
        let attributes = new Map();
        attributes.set(attribute1, value1);
        attributes.set(attribute2, value2);
        attributes.set(attribute3, value3);

        let xhr = new XMLHttpRequest();
        xhr.open("POST", "/rest/entity/new/"+kind+"/"+key);
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        xhr.send(JSON.stringify(attributes));

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
                logout();
            } else {
                alert("FAIL");
            }
        };
        xhr.onerror = function() {
            alert("FAIL");
        };
    });
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
            loadUserInfo(username, userRole);
            document.getElementById("decodedToken").innerHTML = xhr.responseText;
        } else {
            //window.location.href = "/login";
        }
    };
    xhr.onerror = function() {
        console.log("UPS");
        //window.location.href = "/login";
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
function openToken() {
    closeForms();
    document.getElementById("tokenPopup").style.display = "block";
}
function openDecoded() {
    closeForms();
    document.getElementById("decodedPopup").style.display = "block";
}

function openUpdatePForm() {
    closeForms();
    document.getElementById("updatePFormPopup").style.display = "block";
}

function closeForms() {
    document.getElementById("updateAFormPopup").style.display = "none";
    document.getElementById("updateCFormPopup").style.display = "none";
    document.getElementById("updateRFormPopup").style.display = "none";
    document.getElementById("updateSFormPopup").style.display = "none";
    document.getElementById("updatePFormPopup").style.display = "none";
    document.getElementById("tokenPopup").style.display = "none";
    document.getElementById("decodedPopup").style.display = "none";
}

function elementVisibility(username, userRole) {
    const targetname = document.getElementById("username").textContent;
    const targetRole = document.getElementById("role").textContent;

    document.getElementById("updateStatusBTN").style.display = "block";
    document.getElementById("updateRoleBTN").style.display = "block";
    document.getElementById("updateCredentialsBTN").style.display = "none";
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

    if(userRole !== "SU") {
        document.getElementById("updateRoleBTN").style.display = "none";
    }
}

function getToken() {
    let xhr = new XMLHttpRequest();
    xhr.onload = function() {
        if (xhr.status === 200) {
            document.getElementById("token").innerHTML = xhr.responseText;
        }
    };
    xhr.open("GET", "/rest/token", true);
    xhr.send();
}

function downloadPFP() {
    document.getElementsByClassName("profile_picture")[0].src = "https://storage.googleapis.com/universe-202223.appspot.com/" + document.getElementById("username").textContent;
}

document.addEventListener("DOMContentLoaded", function() {
    validateToken();
    updateAttributes();
    updateCredentials();
    updatePassword();
    updateRole();
    updateStatus();
    removeUser();
    logout();
    getToken();
});
