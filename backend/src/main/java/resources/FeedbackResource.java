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
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.FirebaseAuth.authenticateToken;
import static utils.FirebaseAuth.getRole;

/**
 * The FeedbackResource class represents a resource for handling feedback related operations.
 */
@Path("/feedback")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedbackResource {
    private static final Logger LOG = Logger.getLogger(FeedbackResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    /**
     * Endpoint for submitting feedback.
     *
     * @param token The authorization token.
     * @param data  The feedback data.
     * @return A Response object containing the result of the submission.
     * It will return 401 error if there is no token.
     */
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
        if(!data.validate()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }
        return submit(decodedToken, data);
    }

    /**
     * Endpoint for viewing feedback.
     *
     * @param token  The authorization token.
     * @param size   The number of feedback items to retrieve.
     * @param cursor The cursor for pagination.
     * @return A Response object containing the feedback data.
     * It will return 401 error if there is no token.
     */
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

        String role = getRole(decodedToken);
        if(! (role.equals(BO) || role.equals(ADMIN)) ){
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
        }

        return view(cursor, size);
    }

    /**
     * Endpoint for fetching feedback statistics.
     *
     * @param token The authorization token.
     * @return A Response object containing the feedback statistics.
     *         It will return 401 error if there is no token.
     */
    @GET
    @Path("/stats")
    public Response statsFeedback(@HeaderParam("Authorization") String token) {
        LOG.fine("Attempt to fetch feedback");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

       return statusFeedback();
    }

    /**
     * Submits feedback.
     *
     * @param decodedToken The decoded authorization token.
     * @param data         The feedback data.
     * @return A Response object containing the result of the submission.
     */
    private Response submit(FirebaseToken decodedToken, FeedbackData data){
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
                    .set("rating", data.getRating())
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

    /**
     * Retrieves feedback.
     *
     * @param cursor The cursor for pagination.
     * @param size   The number of feedback items to retrieve.
     * @return A Response object containing the feedback data.
     */
    private Response view(String cursor, int size){
        Transaction txn = datastore.newTransaction();

        try {
            EntityQuery.Builder query = Query.newEntityQueryBuilder().setKind("Feedback").setOrderBy(StructuredQuery.OrderBy.desc("submitted")).setLimit(size);

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


    /**
     * Retrieves feedback statistics.
     *
     * @return A Response object containing the feedback statistics.
     */
   private Response statusFeedback(){
       Transaction txn = datastore.newTransaction();

       try {
           EntityQuery.Builder query = Query.newEntityQueryBuilder().setKind("Feedback");

           QueryResults<Entity> results = txn.run(query.build());

           List<Entity> feedbackList = new ArrayList<>();

           float ratingSum = 0;
           float numFeedback = 0;
           while (results.hasNext()) {
               Entity feedback = results.next();
               feedbackList.add(feedback);
               ratingSum += feedback.getLong("rating");
               numFeedback++;
           }

           DecimalFormat df = new DecimalFormat("#.##");
           df.setMaximumFractionDigits(2);

           float[] stats = { Float.parseFloat( df.format(ratingSum/numFeedback) ), numFeedback};

           LOG.info( "Feedback stats fetched");
           txn.commit();

           Gson g = new Gson();

           return Response.ok(g.toJson(stats)).build();
       } finally {
           if (txn.isActive()) {
               txn.rollback();
           }
       }
   }

}
