package models;

public class UserData {
	private static final String ALUNO = "A";
	private static final String UNCHECKED = "EMPTY";

	public String username;
	public String[] multipleEmails;

	public String license_plate;
	public String email;
	public String name;
	public String password;
	public String confirmation;
	public String status;

	public String token;


	public UserData() { }

	public boolean validateRegister() {
		if (email == null || name == null || password == null || confirmation == null) {
			return false;
		}

		if (!email.matches("^[A-Za-z0-9._%+-]+@(fct\\.unl\\.pt|campus\\.fct\\.unl\\.pt)$")) {
			return false;
		}
		if (!password.matches("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,64}")) {
			return false;
		}
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
			return ALUNO;

		if (this.email.contains("@fct.unl.pt"))
			return UNCHECKED;

		return null;
	}
}
