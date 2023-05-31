package util;

import java.util.List;

public class ReportData {
    public String title;
    public String department;

    public boolean validate() {
        return title != null  && department!= null;
    }
}
