function registeredUsers(){
    var xmlhttp = new XMLHttpRequest();

    xmlhttp.open("POST", document.location.origin + "/rest/profile/numberOfUsers", true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState == 4) {
            if(xmlhttp.status == 200) {
                document.getElementById("registered").innerHTML = xmlhttp.responseText;
            }
            else{
                print(xmlhttp.responseText);
                alert("Falhou");
            }
        }
    }

    xmlhttp.send();
}

function repToSolve(){
    var xmlhttp = new XMLHttpRequest();

    xmlhttp.open("GET", document.location.origin + "/rest/reports/unresolved", true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState == 4) {
            if(xmlhttp.status == 200) {
                document.getElementById("unresolvedRep").innerHTML = xmlhttp.responseText;
            }
            else{
                print(xmlhttp.responseText);
                alert("Falhou");
            }
        }
    }

    xmlhttp.send();
}