package services;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

import javax.inject.Singleton;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class BlacklistToken implements ServletContextListener {
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        scheduler.scheduleAtFixedRate(this::removeExpiredTokens, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    private void removeExpiredTokens() {
        Timestamp now = Timestamp.now();
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("Token_Blacklist")
                .setFilter(StructuredQuery.PropertyFilter.le("expiration", now))
                .build();

        QueryResults<Entity> results = datastore.run(query);
        while (results.hasNext()) {
            Entity tokenEntity = results.next();
            Key tokenKey = tokenEntity.getKey();
            datastore.delete(tokenKey);
        }
    }


}
