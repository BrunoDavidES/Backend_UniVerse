package models;

import com.google.cloud.datastore.Entity;

public class ModifyAttributesData {

    public String name;
    public String phone;
    public String status;
    public String privacy;
    public String license_plate;
    public String linkedin;
    public String department;
    public String department_job;
    public String nucleus;
    public String nucleus_job;

    public void fillGaps(Entity targetUser) {
        if(this.name == null)
            this.name = targetUser.getString("name");
        if(this.phone == null)
            this.phone = targetUser.getString("phone");
        if(this.license_plate == null)
            this.license_plate = targetUser.getString("license_plate");
        if(this.status == null)
            this.status = targetUser.getString("status");
        if(this.privacy == null)
            this.privacy = targetUser.getString("privacy");
        if(this.linkedin == null)
            this.linkedin = targetUser.getString("linkedin");
        if(this.department == null)
            this.department = targetUser.getString("department");
        if(this.department_job == null)
            this.department_job = targetUser.getString("department_job");
        if(this.nucleus == null)
            this.nucleus = targetUser.getString("nucleus");
        if(this.nucleus_job == null)
            this.nucleus_job = targetUser.getString("nucleus_job");
    }
}