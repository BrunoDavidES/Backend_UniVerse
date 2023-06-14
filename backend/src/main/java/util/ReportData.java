package util;

import java.util.List;

public class ReportData {
    public String title;

    public String location;

    public boolean validate() {
        return title != null  && location!= null;
    }
}
