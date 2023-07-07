package models;

import com.google.cloud.datastore.*;

public class FeedData {
    // News and Event attributes
    private String title;

    // Event only attributes
    private String startDate;
    private String endDate;
    private String location;
    private String department;
    private String nucleus;
    private String isPublic;
    private String capacity;
    private String isItPaid;
    private String validated_backoffice;

    // News only attributes
    public String authorNameByBO;

    public FeedData() {
    }

    public boolean validate(String kind) {
        if (title == null)
            return false;



        if(kind.equals("Event")) {
            if (department != null && nucleus == null)
                nucleus = "";
            else if (department == null && nucleus != null)
                department = "";
            else
                return false;
            return isPublic != null && isItPaid != null && startDate != null && endDate != null && location != null && Integer.parseInt(capacity) > 1;
        }

        return true;
    }

    public boolean validateEdit(Entity entry, String kind){
        if (title == null){
            title = entry.getString("title");
        } else if (title.equals("")) return false;

        if (validated_backoffice == null){
            validated_backoffice = entry.getString("validated_backoffice");
        }
        else if (!validated_backoffice.equals("true") && !validated_backoffice.equals("false")) return false;


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

        if (isPublic == null){
            isPublic = entry.getString("isPublic");
        }
        else if (!isPublic.equals("yes") && !isPublic.equals("no")) return false;

        if (capacity == null){
            capacity = entry.getString("capacity");
        }
        else if (Integer.parseInt(capacity) < 2) return false;



        if (isItPaid == null){
            isItPaid = entry.getString("isItPaid");
            return true;
        }

        return isItPaid.equals("no") || isItPaid.equals("yes");

    }

    public String getTitle() {
        return title;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getLocation() {
        return location;
    }

    public String getDepartment() {
        return department;
    }

    public String getNucleus() {
        return nucleus;
    }

    public String getIsPublic() {
        return isPublic;
    }

    public String getCapacity() {
        return capacity;
    }

    public String getIsItPaid() {
        return isItPaid;
    }

    public String getValidated_backoffice() {
        return validated_backoffice;
    }

    public String getAuthorNameByBO() {
        return authorNameByBO;
    }


}
