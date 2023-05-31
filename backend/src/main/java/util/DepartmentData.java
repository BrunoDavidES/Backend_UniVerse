package util;

import com.google.cloud.datastore.Entity;

import java.util.List;
import java.util.Map;

public class DepartmentData {


    public String id;
    public List<String> members;
    public String email;
    public String name;
    public String president;
    public String location;
    public String phoneNumber;
    public String address;
    public String fax;


    public DepartmentData() { }

    public boolean validateRegister() {
        if (id == null || email == null || name == null || president == null || address == null || phoneNumber == null) {
            return false;
        }

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
            if (!email.matches("^[A-Za-z0-9._%+-]+@@(fct\\.unl\\.pt)$")) {
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
        if(this.location == null)
            this.location = department.getString("location");
        if(this.phoneNumber == null)
            this.phoneNumber = department.getString("phoneNumber");
        if(this.address == null)
            this.address = department.getString("address");
        if(this.fax == null)
            this.fax = department.getString("fax");
    }
}
