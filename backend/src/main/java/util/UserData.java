package util;

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
	public String[][] attributes;

	
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



}
