package models;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;
import  com.google.cloud.datastore.*;

import java.util.List;
import java.util.Map;

public class DepartmentData {
    private String id;  //acronimo do departamento, exemplo MIEI
    private String email;
    private String name;
    private String president;
    private String phoneNumber;
    private String location;
    private String fax;

    public DepartmentData() { }

    public boolean validateRegister() {

        if (id == null || email == null || name == null || president == null ||  location == null || phoneNumber == null) {
            return false;
        }
        if (!id.matches(".{3,64}")){
            return false;
        }

        return email.matches("^[A-Za-z0-9._%+-]+@(fct\\.unl\\.pt)$");
    }

    public boolean validateModify() {
        if(this.email != null)
            if (!email.matches("^[A-Za-z0-9._%+-]+@(fct\\.unl\\.pt)$")) {
                return false;
            }
        return id != null ;
    }
    public void fillGaps(Entity department) {
        if(this.name == null || this.name.equals(""))
            this.name = department.getString("name");
        if(this.email == null || this.email.equals(""))
            this.email = department.getString("email");
        if(this.president == null || this.president.equals(""))
            this.president = department.getString("president");
        if(this.phoneNumber == null || this.phoneNumber.equals(""))
            this.phoneNumber = department.getString("phoneNumber");
        if(this.location == null || this.location.equals(""))
            this.location = department.getString("location");
        if(this.fax == null || this.fax.equals(""))
            this.fax = department.getString("fax");
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPresident() {
        return president;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLocation() {
        return location;
    }

    public String getFax() {
        return fax;
    }
}