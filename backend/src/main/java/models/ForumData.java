package models;

public class ForumData {
    private String name;
    private String password;
    private String message;

    public ForumData() {
    }

    public boolean validateCreation() {
        return name != null;
    }

    public boolean validatePost() {
        return message != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
