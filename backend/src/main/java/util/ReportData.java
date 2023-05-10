package util;

import java.util.List;

public class ReportData {
    public String title;
    public String reporter;
    public String department;

    public boolean validate() {
        return title != null && reporter != null  && department!= null;
    }
}
