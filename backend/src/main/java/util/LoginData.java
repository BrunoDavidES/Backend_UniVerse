package util;

import org.apache.commons.codec.digest.DigestUtils;

public class LoginData {

    public String username;
    public String password;

    public LoginData() { }

    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean isPasswordValid(String userHashedPassword) {
        String hashedPassword = DigestUtils.sha512Hex(this.password);

        return hashedPassword.equals(userHashedPassword);
    }
}
