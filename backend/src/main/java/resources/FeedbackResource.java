package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import models.FeedbackData;
import models.ReportData;
import utils.QueryResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.FirebaseAuth.authenticateToken;
import static utils.FirebaseAuth.getRole;

@Path("/feedback")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedbackResource {
    private static final Logger LOG = Logger.getLogger(FeedbackResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response submitFeedback(@HeaderParam("Authorization") String token,
                                   FeedbackData data){
        LOG.fine("Attempt to post feedback.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        /*if(!data.validate()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }*/

        Transaction txn = datastore.newTransaction();

        try {
            Key key;
            Entity entity;
            String id;
            do {
                id = UUID.randomUUID().toString();
                key = datastore.newKeyFactory().setKind("Feedback").newKey(id);
                entity = txn.get(key);
            } while(entity != null);

            Entity.Builder builder = Entity.newBuilder(key);

            builder.set("author", decodedToken.getUid())
                    .set("message", data.getMessage())
                    .set("submitted", Timestamp.now());

            entity = builder.build();
            txn.add(entity);

            LOG.info("Feedback submitted " + id);
            txn.commit();
            return Response.ok(id).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    // TODO check pagination
    @GET
    @Path("/view")
    public Response viewFeedback(@HeaderParam("Authorization") String token,
                                 @QueryParam("size") int size,
                                 @QueryParam("cursor") String cursor){
        LOG.fine("Attempt to fetch feedback");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        if(getRole(decodedToken) == "EXAMPLE") {
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            EntityQuery.Builder query = Query.newEntityQueryBuilder().setKind("Feedback").setLimit(size);

            if (!cursor.equals("EMPTY") && !cursor.equals("")){
                query.setStartCursor(Cursor.fromUrlSafe(cursor));
            }

            QueryResults<Entity> results = txn.run(query.build());

            List<Entity> feedbackList = new ArrayList<>();

            while (results.hasNext()) {
                Entity feedback = results.next();
                feedbackList.add(feedback);
            }

            LOG.info( "Feedback fetched");
            txn.commit();

            QueryResponse response = new QueryResponse();
            response.setResults(feedbackList);
            response.setCursor(results.getCursorAfter().toUrlSafe());

            Gson g = new Gson();
            return Response.ok(g.toJson(response)).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

}
