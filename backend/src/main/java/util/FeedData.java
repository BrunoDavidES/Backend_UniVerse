package util;

import com.google.cloud.datastore.*;

public class FeedData {

    // News and Event attributes
    public String title;

    public String author;

    // Event only attributes
    public String startDate;

    public String endDate;

    public String location;

    public String department;

    public String isPublic;

    public int capacity;

    public String isItPaid;

    public boolean validate(String kind) {
        if (title == null)
            return false;

        if (author == null)
            return false;

        if (department == null)
            this.department = "ᓚᘏᗢ  EMPTY  ᓚᘏᗢ";

        if(isPublic == null)
            this.isPublic = "PUBLIC";

        if(isItPaid == null)
            this.isItPaid = "NOT_PAID";

        if(kind.equals("Event")) {
            return startDate != null && endDate != null && location != null && capacity > 0;
        }

        return true;
    }

    public boolean validateEdit(Entity entry, String kind){
        if (title == null){
            title = entry.getString("title");
        }
        else if (title.equals("")) return false;

        if (author == null){
            author = entry.getString("author");
        }
        else if (author.equals("")) return false;

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
        } else if (!isPublic.equals("PRIVATE") && !isPublic.equals("PUBLIC")) return false;

        if (capacity < 2)
            return false;

        if (isItPaid == null){
            isItPaid = entry.getString("isItPaid");
        }
        return isItPaid.equals("NOT_PAID") || isItPaid.equals("PAID");
    }

}
