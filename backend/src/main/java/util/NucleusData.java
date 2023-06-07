package util;

import com.google.cloud.datastore.Entity;

import java.util.List;

public class NucleusData {
    // Create
    public String name;

    // Aronym ex: ninf
    public String id;

    public String president;

    public String nucleusEmail;

    // Modify

    public String newName;

    public List<String> members;

    public String website;

    public String instagram;

    public String twitter;

    public String facebook;

    public String youtube;

    public String linkedIn;

    public String description;

    public NucleusData(){}


    public boolean validateRegister() {
        if (name == null || id == null || president == null || nucleusEmail == null) {
            return false;
        }
        return nucleusEmail.matches("^[A-Za-z0-9._%+-]+@(ae+\\.fct\\.unl\\.pt$)");
    }

    public boolean validateModify(){
        return name != null;
    }

    public void fillGaps(Entity nucleus){
        if (newName == null) newName = nucleus.getString("name");
        if (id == null) id = nucleus.getString("id");
        if (president == null) president = nucleus.getString("president");
        if (nucleusEmail == null) nucleusEmail = nucleus.getString("nucleusEmail");
        if (website == null) website = nucleus.getString("website");
        if (instagram == null) instagram = nucleus.getString("instagram");
        if (twitter == null) twitter = nucleus.getString("twitter");
        if (facebook == null) facebook = nucleus.getString("facebook");
        if (youtube == null) youtube = nucleus.getString("youtube");
        if (linkedIn == null) linkedIn = nucleus.getString("linkedIn");
        if (description == null) description = nucleus.getString("description");
    }

    public boolean validateList(){
        return this.members == null || this.members.isEmpty();
    }

}
