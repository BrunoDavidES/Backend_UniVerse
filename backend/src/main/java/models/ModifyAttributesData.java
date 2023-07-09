package models;

import com.google.cloud.datastore.Entity;

public class ModifyAttributesData {
    private String name;
    private String phone;
    private String status;
    private String privacy;
    private String license_plate;
    private String linkedin;
    private String department;
    private String department_job;
    private String nucleus;
    private String nucleus_job;
    private String office;

    public ModifyAttributesData() {
    }

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
        if(this.office == null)
            this.office = targetUser.getString("office");
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }

    public String getPrivacy() {
        return privacy;
    }

    public String getLicense_plate() {
        return license_plate;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public String getDepartment() {
        return department;
    }

    public String getDepartment_job() {
        return department_job;
    }

    public String getNucleus() {
        return nucleus;
    }

    public String getNucleus_job() {
        return nucleus_job;
    }

    public String getOffice() {
        return office;
    }


}