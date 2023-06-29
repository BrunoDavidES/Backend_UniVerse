package models;

import com.google.cloud.datastore.Entity;

public class ModifyAttributesData {

    public String name;
    public String status;
    public String license_plate;
    public void fillGaps(Entity targetUser) {
        if(this.name == null)
            this.name = targetUser.getString("name");
        if(this.license_plate == null)
            this.license_plate = targetUser.getString("license_plate");
        if(this.status == null)
            this.status = targetUser.getString("status");
    }
}
