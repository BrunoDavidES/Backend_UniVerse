package util;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;

import java.util.List;

public class NucleusData {
    // Create
    public String name;

    // Aronym ex: ninf
    public String id;

    public String president;

    public String nucleusEmail;

    // Modify
    public String location;
    public String newName;


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
}