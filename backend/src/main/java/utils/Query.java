package utils;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.*;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Query {

    public Query() {}

    public static CompositeFilter CompositeFilterAnd(Map<String, String> filters) {
        CompositeFilter attributeFilter = null;
        PropertyFilter propFilter;

        if( filters == null){
            return null;
        }

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            propFilter = PropertyFilter.eq(entry.getKey(), entry.getValue());

            if(attributeFilter == null)
                attributeFilter = CompositeFilter.and(propFilter);
            else
                attributeFilter = CompositeFilter.and(attributeFilter, propFilter);
        }
        return attributeFilter;
    }

    public static int count(QueryResults<Entity> queryResults) {
        int count = 0;
        while (queryResults.hasNext()) {
            queryResults.next();
            count++;
        }
        return count;
    }

    public static String toJson(QueryResults<Entity> queryResults) {
        List<Entity> results = new ArrayList<>();
        queryResults.forEachRemaining(results::add);

        Gson gson = new Gson();
        return gson.toJson(results);
    }


}
