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

	public UserData(String username, String password, String confirmation, String email, String name) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.confirmation = confirmation;
		this.name = name;
	}

	public boolean validateRegister() {
		if (username == null || email == null || name == null || password == null || confirmation == null) {
			return false;
		}
		if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$")) {
			return false;
		}
		if (!password.matches("(?=.*[0-9])(?=.*[A-Z]).{6,}")) {
			return false;
		}
		return password.equals(confirmation);
	}

	public String[] getEmails() {
		return multipleEmails;
	}

	public boolean validateLogin() {
		if (username == null || password == null) {
			return false;
		}
		return true;
	}



}
