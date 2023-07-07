package models;

import com.google.cloud.datastore.Entity;

import static utils.Constants.*;

public class ModifyRoleData {
    private String target;
    private String newRole;
    private String office;
    private String department;

    public String department_job;

    public boolean validatePermission(String modifierRole, String targetRole, Entity targetUser) {

        if(this.department == null || this.department.equals(""))
            this.department = targetUser.getString("department");
        if(this.department_job == null || this.department_job.equals(""))
            this.department_job = targetUser.getString("department_job");

        if (modifierRole.equals(ADMIN))
            return true;
        if(modifierRole.equals(BO) && (!targetRole.equals(BO) && !targetRole.equals(ADMIN)))
            return true;

        return modifierRole.equals(TEACHER) && targetRole.equals(STUDENT) && newRole.equals(TEACHER);
    }

    public boolean validateDelete(String modifierRole, String targetRole) {
        return modifierRole.equals(ADMIN) || (modifierRole.equals(BO) && !targetRole.equals(BO) && !targetRole.equals(ADMIN));
    }

    public String getTarget() {
        return target;
    }

    public String getNewRole() {
        return newRole;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public String getDepartment() {
        return department;
    }

    public String getDepartment_job() {
        return department_job;
    }


}
