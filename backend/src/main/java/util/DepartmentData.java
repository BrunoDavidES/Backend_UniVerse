package util;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;
import  com.google.cloud.datastore.*;

import java.util.List;
import java.util.Map;

public class DepartmentData {


    public String id;  //acronimo do departamento, exemplo MIEI
    public List<String> members;
    public String email;
    public String name;
    public String president;
    public String phoneNumber;
    public LatLng location;
    public double latitude;
    public double longitude;
    public String fax;


    public DepartmentData() { }

    public boolean validateRegister() {

        if (id == null || email == null || name == null || president == null || latitude == 0 || longitude == 0 || phoneNumber == null) {
            return false;
        }
        this.location = LatLng.of(this.latitude,this.longitude);
        if (!id.matches(".{3,64}")){
            return false;
        }

        return email.matches("^[A-Za-z0-9._%+-]+@(fct\\.unl\\.pt)$");
    }
    public boolean validateList(){
        return this.members == null || this.members.isEmpty();
    }

    public boolean validateModify() {
        if(this.email != null)
            if (!email.matches("^[A-Za-z0-9._%+-]+@(fct\\.unl\\.pt)$")) {
                return false;
            }
        return id != null ;
    }
    public void fillGaps(Entity department) {
        if(this.name == null)
            this.name = department.getString("name");
        if(this.email == null)
            this.email = department.getString("email");
        if(this.president == null)
            this.president = department.getString("president");
        if(this.phoneNumber == null)
            this.phoneNumber = department.getString("phoneNumber");
        if(this.latitude == 0 || this.longitude == 0)
            this.location = department.getLatLng("location");
        else
            this.location = LatLng.of(this.latitude,this.longitude);
        if(this.fax == null)
            this.fax = department.getString("fax");
    }
}