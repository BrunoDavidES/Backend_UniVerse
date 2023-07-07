package models;

public class ModifyPwdData {
    private String password;
    private String newPwd;
    private String confirmation;

    public ModifyPwdData() { }

    public boolean validatePwd() {
        if ( newPwd == null || password == null) {
            return false;
        }
        if (!newPwd.matches("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,64}")) {
            return false;
        }
        return newPwd.equals(confirmation);
    }

    public String getPassword() {
        return password;
    }

    public String getNewPwd() {
        return newPwd;
    }

    public String getConfirmation() {
        return confirmation;
    }


}
