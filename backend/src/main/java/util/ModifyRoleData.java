package util;

import com.google.cloud.datastore.Entity;

public class ModifyRoleData {
    private static final String BO = "BO";
    private static final String TEACHER = "T";
    private static final String WORKER = "W";
    private static final String STUDENT = "S";
    private static final String ADMIN = "A";

    public String target;

    public String newRole;

    public String office;

    public String department;

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
}
