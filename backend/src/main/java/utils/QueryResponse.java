package utils;

import com.google.cloud.datastore.Entity;

import java.util.List;

public class QueryResponse {
    private List<Entity> results;
    private String cursor;

    public QueryResponse() {
    }

    public void setResults(List<Entity> results) {
        this.results = results;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}