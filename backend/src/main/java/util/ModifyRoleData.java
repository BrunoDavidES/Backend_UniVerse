package util;

public class ModifyRoleData {

    public String modifier;

    public String target;

    public String newRole;

    public boolean validatePermission(String modifierRole, String targetRole) {
        if(modifierRole.equals("BO") && !targetRole.equals("BO"))
            return true;
        return modifierRole.equals("D") && targetRole.equals("A") && newRole.equals("D");
    }
}
