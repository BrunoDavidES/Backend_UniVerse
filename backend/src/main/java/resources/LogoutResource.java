package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import util.ValToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {
    private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public LogoutResource() {
        scheduler.scheduleAtFixedRate(this::removeExpiredTokens, 0, 1, TimeUnit.HOURS);
    }

    @POST
    @Path("/")
    public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        LOG.fine("Attempt to logout user.");

        Transaction txn = datastore.newTransaction();

        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.invalidateToken(request, response);

            Key tokenKey = datastore.newKeyFactory().setKind("Token_Blacklist").newKey(token.getId());;

            Entity blToken = Entity.newBuilder(tokenKey)
                    .set("expiration", Timestamp.of(token.getExpiresAt()))
                    .build();
            txn.add(blToken);


            LOG.info("User logged out");
            txn.commit();
            return Response.ok().build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    private void removeExpiredTokens() {
        LOG.fine("Checking for expired tokens.");

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

        LOG.fine("Expired tokens removed.");
    }


}
