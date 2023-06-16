package util;

import com.google.cloud.datastore.Entity;

public class ModifyAttributesData {

    public String name;
    public String status;
    public void fillGaps(Entity targetUser) {
        if(this.name == null)
            this.name = targetUser.getString("name");
        if(this.status == null)
            this.status = targetUser.getString("status");
    }
}
