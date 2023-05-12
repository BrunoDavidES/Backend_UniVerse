package util;

import java.util.List;

public class FeedData {

    // News and Event attributes
    public String title;

    // Event only attributes
    public String startDate;
    public String endDate;
    public List<String> manager;
    public String location;
    public int capacity;
    public boolean isItPaid;

    public boolean validate(String kind) {
        boolean validity = title != null;

        if(kind.equals("Event")) {
            validity = validity && startDate != null && endDate != null && manager != null && location != null && capacity > 0;
        }
        return validity;
    }

}
