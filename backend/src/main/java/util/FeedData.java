package util;

import java.util.List;

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

}
