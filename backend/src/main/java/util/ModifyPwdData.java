package util;

public class ModifyPwdData {
    public String username;
    public String password;
    public String newPwd;
    public String confirmation;
    public ModifyPwdData() { }

    public boolean validatePwd() {
        if (username == null || newPwd == null || password == null) {
            return false;
        }
        if (!newPwd.matches("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,64}")) {
            return false;
        }
        return newPwd.equals(confirmation);
    }
}
