package util;

import java.util.List;

public class NucleusData {
    // Create
    public String name;

    public String acronym;

    public String creatorEmail;

    public String nucleusEmail;

    // Modify
    public String president;

    public String modifierEmail;

    public List<String> members;

    public String website;

    public List<String> socials;

    public String description;

    public NucleusData(){}


    public boolean validateRegister() {
        if (name == null || acronym == null || creatorEmail == null || nucleusEmail == null || president == null) {
            return false;
        }
        if (!nucleusEmail.matches("^[A-Za-z0-9._%+-]+@(ae+\\.fct\\.unl\\.pt$)")) {
            return false;
        }
        return creatorEmail.matches("^[A-Za-z0-9._%+-]+@(campus+\\.fct\\.unl\\.pt$)");
    }

    public boolean validateModify() {
        if(this.modifierEmail != null)
            return this.modifierEmail.matches("^[A-Za-z0-9._%+-]+@(campus\\.fct\\.unl\\.pt$)");
        return false;
    }
}
