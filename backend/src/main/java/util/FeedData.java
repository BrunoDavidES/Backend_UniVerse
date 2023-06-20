package util;

import com.google.cloud.datastore.*;

public class FeedData {

    // News and Event attributes
    public String title;

    // Event only attributes
    public String startDate;

    public String endDate;

    public String location;

    public String department;

    public String isPublic;

    public String capacity;

    public String isItPaid;

    public boolean validate(String kind) {
        if (title == null)
            return false;

        if (department == null)
            this.department = "ᓚᘏᗢ  EMPTY  ᓚᘏᗢ";

        if(isPublic == null)
            this.isPublic = "No";

        if(isItPaid == null)
            this.isItPaid = "No";

        if(kind.equals("Event")) {
            return startDate != null && endDate != null && location != null && Integer.parseInt(capacity) > 1;
        }

        return true;
    }

    public boolean validateEdit(Entity entry, String kind){
        if (title == null){
            title = entry.getString("title");
        }
        else if (title.equals("")) return false;

        if (kind.equals("News"))
            return true;

        //Decidir formato das datas
        if (startDate == null){
            startDate = entry.getString("startDate");
        }
        else if (startDate.equals("")) return false;

        if (endDate == null){
            endDate = entry.getString("endDate");
        }
        else if (endDate.equals("")) return false;

        //Decidir formato da localização, se é só o nome ou coordenadads, ou ambos
        if (location == null){
            location = entry.getString("location");
        }
        else if (location.equals("")) return false;

        if (department == null){
            department = entry.getString("department");
        }
        else if (department.equals("")) return false;

        if (isPublic == null){
            isPublic = entry.getString("isPublic");
        } else if (!isPublic.equals("No") && !isPublic.equals("Yes")) return false;

        if (capacity == null){
            capacity = entry.getString("capacity");
        }
        else if (Integer.parseInt(capacity) < 2) return false;

        if (isItPaid == null){
            isItPaid = entry.getString("isItPaid");
            return true;
        }
        return isItPaid.equals("No") || isItPaid.equals("Yes");

    }

}
