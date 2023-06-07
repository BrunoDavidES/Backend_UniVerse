package util;

import java.util.List;

public class FeedData {

    // News and Event attributes
    public String title;

    public String author;

    // Event only attributes
    public String startDate;

    public String endDate;

    public List<String> manager;

    public String location;

    public String department;

    public String isPublic;

    public int capacity;

    public boolean isItPaid;

    public boolean validate(String kind) {
        if (title == null)
            return false;

        if(kind.equals("Event")) {
            return startDate != null && endDate != null && manager != null && location != null && capacity > 0;
        }

        return true;
    }

}
