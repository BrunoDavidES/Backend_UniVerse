package util;

public class UserData {
	public String username;
	public String email;
	public String name;
	public String password;
	public String confirmation;

	public String role;

	
	public UserData() { }

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

	public boolean validateLogin() {
		if (username == null || password == null) {
			return false;
		}
		return true;
	}



}
