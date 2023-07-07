package models;

import com.google.cloud.datastore.Entity;

import java.util.HashMap;
import java.util.Map;

public class UserData {
	private static final String STUDENT = "S";
	private static final String WORKER = "W";
	private static final String UNCHECKED = "EMPTY";

	public String username;
	public String[] multipleEmails;

	public String license_plate;
	public String email;
	public String name;
	public String password;
	public String confirmation;
	public String status;


	public UserData() { }

	public boolean validateRegister() {
		if (email == null || name == null || password == null || confirmation == null) {
			return false;
		}

		if (!email.matches("^[A-Za-z0-9._%+-]+@(fct\\.unl\\.pt|campus\\.fct\\.unl\\.pt)$")) {
			return false;
		}

		/* TESTE DO BRUNO
		if (!password.matches("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,64}")) {
			return false;
		}

		*/

		this.username = email.split("@")[0];
		return password.equals(confirmation);
	}

	public String[] getEmails() {
		return multipleEmails;
	}

	public boolean validateLogin() {

		return username != null && password != null;
	}

	public String getRole() {
		if(this.email.contains("@campus.fct.unl.pt")) //ver se email é só @fct.unl.pt, e como ver se é proff ou funcionário
			return STUDENT;

		if (this.email.contains("@fct.unl.pt"))
			return WORKER;

		return null;
	}
}
