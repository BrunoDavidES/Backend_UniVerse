package util;

public class ModifyRoleData {

    public String target;

    public String newRole;

    public String office;

    public boolean validatePermission(String modifierRole, String targetRole) {
        if(modifierRole.equals("BO") && !targetRole.equals("BO"))
            return true;
        return modifierRole.equals("D") && targetRole.equals("A") && newRole.equals("D");
    }

    public boolean validateDelete(String modifierRole, String targetRole) {
        return modifierRole.equals("BO") && !targetRole.equals("BO");
    }
}
