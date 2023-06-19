 let popup = document.getElementById("popup");

  function openPopup(){
    popup.classList.add("open-popup");
  }

  function closePopup(){
    popup.classList.remove("open-popup");
  }

  let sidebar = document.querySelector(".sidebar");
  let sidebarBtn = document.querySelector(".sidebarBtn");
  sidebarBtn.onclick = function() {
    sidebar.classList.toggle("active");
    if(sidebar.classList.contains("active")){
      sidebarBtn.classList.replace("bx-menu" ,"bx-menu-alt-right");
    }else
      sidebarBtn.classList.replace("bx-menu-alt-right", "bx-menu");
   }

  function loadLoggedUser() {
    var xmlhttp = new XMLHttpRequest();
	var user = localStorage.getItem("userLogged");

	var postLogged = {
		"username": user
	}

	var json = JSON.stringify(postLogged);

	xmlhttp.onreadystatechange = function() {
	  if (xmlhttp.readyState == 4) {
	    if (xmlhttp.status == 200) {
			var userLogged = JSON.parse(this.responseText);

			document.getElementById("name").innerHTML = userLogged.name;

			xmlhttp.open("POST", document.location.origin + "/rest/profile/", true);
			xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
			xmlhttp.send(json);
		}
	  }
	}

	function goToLogin(){
	  if(localStorage.getItem("userLogged") === ""){
	    window.location.href = "/index.html";
      }
	}

  function logout(){
	var xmlhttp = new XMLHttpRequest();

    var user = document.getElementById("user").innerHTML;

    var postLogout = {
      "username": user
    }

    var json = JSON.stringify(postLogout);

    xmlhttp.onreadystatechange = function() {
      if(xmlhttp.readyState == 4) {
	    if(xmlhttp.status == 200) {
		  localStorage.setItem("userLogged", "");
		  window.location.href = "/pages/login.html";
		}
		else{
		  xmlhttp.responseText;
		}
      }
    }

	xmlhttp.open("POST", document.location.origin + "/rest/logout", true);
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	xmlhttp.send(json);
  }