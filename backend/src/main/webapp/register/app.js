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
            let landline = document.getElementById("landline").value;
            let mobile = document.getElementById("mobile").value;
            let occupation = document.getElementById("occupation").value;
            let workplace = document.getElementById("workplace").value;
            let address = document.getElementById("address").value;
            let complementary = document.getElementById("complementary").value;
            let city = document.getElementById("city").value;
            let postcode = document.getElementById("postcode").value;
            let nif = document.getElementById("nif").value;
            let privacy = document.getElementById("privacy").value;
            let userData = {
                "username": username,
                "name": name,
                "email": email,
                "password": password,
                "confirmation": confirmation,
                "privacy": privacy,
                "landline": landline,
                "mobile": mobile,
                "occupation": occupation,
                "workplace": workplace,
                "address": address,
                "complementary": complementary,
                "city": city,
                "postcode": postcode,
                "nif": nif
            };
            let xhr = new XMLHttpRequest();

            xhr.open("POST", "/rest/register", true);
            xhr.setRequestHeader("Content-Type", "application/json");

            xhr.send(JSON.stringify(userData));
            xhr.onload = function () {
                if (xhr.status === 200) {
                    uploadPFP();
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

function validateToken() {
    let xhr = new XMLHttpRequest();
    xhr.open("GET", "/rest/token/validate", true);
    xhr.send();
    xhr.onload = function() {
        if (xhr.status === 200) {
            window.location.href = "/profile";
        }
    };
}

function updateProfilePic() {
    let selectedFile = document.getElementById("profilePic").files[0];
    document.getElementsByClassName("profile_picture")[0].src = URL.createObjectURL(selectedFile);
}

function uploadPFP() {
    let fileInput = document.getElementById("profilePic");
    let file = fileInput.files[0];

    if(file !== null) {
        let xhr = new XMLHttpRequest();
        xhr.open("POST", "/gcs/braided-turbine-379313.appspot.com/" + document.getElementById("username").value, false);
        xhr.setRequestHeader("Content-Type", file.type);
        xhr.send(file);
    }
}


document.addEventListener("DOMContentLoaded", function() {
    validateToken();
    register();
    uploadPFP();
});