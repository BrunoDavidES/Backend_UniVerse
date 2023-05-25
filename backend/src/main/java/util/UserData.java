package util;

import com.google.cloud.datastore.Entity;

import java.util.HashMap;
import java.util.Map;

public class UserData {
	public String username;
	public String[] multipleEmails;
	public String email;
	public String name;
	public String password;
	public String confirmation;
	public String role;
	public String status;

	
	public UserData() { }

	public boolean validateRegister() {
		if (username == null || email == null || name == null || password == null || confirmation == null) {
			return false;
		}
		if (!email.matches("^[A-Za-z0-9._%+-]+@([\\w-]+\\.fct\\.unl\\.pt$)")) {
			return false;
		}
		if (!password.matches("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,64}")) {
			return false;
		}
		return password.equals(confirmation);
	}
	public boolean validateModify() {
		if(this.email != null)
			if (!email.matches("^[A-Za-z0-9._%+-]+@([\\w-]+\\.fct\\.unl\\.pt$)")) {
				return false;
			}
		return username != null && password != null;
	}

	public String[] getEmails() {
		return multipleEmails;
	}

	public boolean validateLogin() {
		return username != null && password != null;
	}
	public void fillGaps(Entity targetUser) {
		if(this.name == null)
			this.name = targetUser.getString("name");
		if(this.email == null)
			this.email = targetUser.getString("email");
		if(this.status == null)
			this.status = targetUser.getString("status");
	}


	public String getRole() {
		if(this.email.contains("@campus.fct.unl.pt")) //ver se email é só @fct.unl.pt, e como ver se é proff ou funcionário
			return "aluno";
		else
			return "docente";
	}
}
