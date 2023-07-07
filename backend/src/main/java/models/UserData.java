package models;

import static utils.Constants.*;

public class UserData {
	private String username;
	private String[] multipleEmails;
	private String license_plate;
	private String email;
	private String name;
	private String password;
	private String confirmation;
	private String status;

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

	public String getUsername() {
		return username;
	}

	public String[] getMultipleEmails() {
		return multipleEmails;
	}

	public String getLicense_plate() {
		return license_plate;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public String getConfirmation() {
		return confirmation;
	}

	public String getStatus() {
		return status;
	}


}
