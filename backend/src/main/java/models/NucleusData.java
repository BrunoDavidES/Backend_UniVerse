package models;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;

import java.util.List;

public class NucleusData {
    // Create
    private String name;

    // Aronym ex: ninf
    private String id;
    private String president;
    private String nucleusEmail;

    // Modify
    private String location;
    private String newName;
    private String website;
    private String instagram;
    private String twitter;
    private String facebook;
    private String youtube;
    private String linkedIn;
    private String description;

    public NucleusData(){}

    public boolean validateRegister() {
        if (name == null || id == null || president == null || nucleusEmail == null) {
            return false;
        }
        return nucleusEmail.matches("^[A-Za-z0-9._%+-]+@(ae+\\.fct\\.unl\\.pt$)");
    }

    public boolean validateModify(){
        if(this.nucleusEmail != null &&  !nucleusEmail.matches("^[A-Za-z0-9._%+-]+@(ae+\\.fct\\.unl\\.pt$)"))
            return false;
        return id != null;
    }

    public void fillGaps(Entity nucleus){
        if (newName == null || newName.equals("")) newName = nucleus.getString("name");
        if (id == null || id.equals("")) id = nucleus.getString("id");
        if (president == null || president.equals("")) president = nucleus.getString("president");
        if (location == null || location.equals("")) location = nucleus.getString("location");
        if (nucleusEmail == null || nucleusEmail.equals("")) nucleusEmail = nucleus.getString("email");
        if (website == null || website.equals("")) website = nucleus.getString("website");
        if (instagram == null || instagram.equals("")) instagram = nucleus.getString("instagram");
        if (twitter == null || twitter.equals("")) twitter = nucleus.getString("twitter");
        if (facebook == null || facebook.equals("")) facebook = nucleus.getString("facebook");
        if (youtube == null || youtube.equals("")) youtube = nucleus.getString("youtube");
        if (linkedIn == null || linkedIn.equals("")) linkedIn = nucleus.getString("linkedIn");
        if (description == null || description.equals("")) description = nucleus.getString("description");
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getPresident() {
        return president;
    }

    public String getNucleusEmail() {
        return nucleusEmail;
    }

    public String getLocation() {
        return location;
    }

    public String getNewName() {
        return newName;
    }

    public String getWebsite() {
        return website;
    }

    public String getInstagram() {
        return instagram;
    }

    public String getTwitter() {
        return twitter;
    }

    public String getFacebook() {
        return facebook;
    }

    public String getYoutube() {
        return youtube;
    }

    public String getLinkedIn() {
        return linkedIn;
    }

    public String getDescription() {
        return description;
    }


}