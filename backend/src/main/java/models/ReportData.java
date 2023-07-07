package models;


public class ReportData {
    private String title;
    private String location;

    public ReportData() {
    }

    public boolean validate() {
        return title != null  && location!= null;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }


}
