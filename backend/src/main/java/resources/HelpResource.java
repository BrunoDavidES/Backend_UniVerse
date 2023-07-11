package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import models.HelpData;
import models.ReportData;
import utils.QueryResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.FirebaseAuth.authenticateToken;
import static utils.FirebaseAuth.getRole;

/**
 * Resource class for managing help requests.
 */
@Path("/help")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class HelpResource {
    private static final Logger LOG = Logger.getLogger(HelpResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    /**
     * Creates a new help request.
     *
     * @param data The help request data.
     * @return Response indicating the status of the help request.
     */
    @POST
    @Path("/request")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestHelp(HelpData data){
        LOG.fine("Attempt to request help.");
        Transaction txn = datastore.newTransaction();

        try {
            Key key; Entity entity; String id;
            do {
                id = (Long.MAX_VALUE - Instant.now().getEpochSecond())+ UUID.randomUUID().toString();
                key = datastore.newKeyFactory().setKind("Help").newKey(id);
                entity = txn.get(key);
            } while(entity != null);

            Entity.Builder builder = Entity.newBuilder(key);

            builder.set("email", data.getEmail())
                    .set("title", data.getTitle())
                    .set("message", data.getMessage())
                    .set("replied", "")
                    .set("submitted", Timestamp.now());

            entity = builder.build();
            txn.add(entity);

            LOG.info("Help request submitted " + id);
            txn.commit();
            return Response.ok(id).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


    /**
     * Answers a help request.
     *
     * @param token     The authorization token.
     * @param requestID The ID of the help request to answer.
     * @param data      The help request data.
     * @return Response indicating the status of the help request answer.
     */
    @POST
    @Path("/{requestID}/answer")
    public Response answerHelp(@HeaderParam("Authorization") String token,
                               @PathParam("requestID") String requestID,
                               HelpData data) {
        LOG.fine("Attempt to answer help request.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        Transaction txn = datastore.newTransaction();
        try {
            Key key = datastore.newKeyFactory().setKind("Help").newKey(requestID);
            Entity entity = txn.get(key);

            if(entity == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(TOKEN_NOT_FOUND).build();
            }

            entity = Entity.newBuilder(entity)
                    .set("replied", Timestamp.now())
                    .build();
            txn.put(entity);

            LOG.info("Help request answered " + requestID);
            txn.commit();
            return Response.ok(requestID).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Fetches all help requests.
     *
     * @param token  The authorization token.
     * @param size   The number of requests to fetch.
     * @param cursor The cursor for pagination.
     * @return Response containing the fetched help requests.
     */
    @GET
    @Path("/view")
    public Response viewRequests(@HeaderParam("Authorization") String token,
                               @QueryParam("size") int size,
                               @QueryParam("cursor") String cursor) {
        LOG.fine("Attempt to fetch help requests");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        return queryRequests(cursor, size);
    }

    /**
     * Fetches unanswered help requests.
     *
     * @param token  The authorization token.
     * @param size   The number of requests to fetch.
     * @param cursor The cursor for pagination.
     * @return Response containing the fetched unanswered help requests.
     */
    @GET
    @Path("/view/unanswered")
    public Response viewUnansweredRequests(@HeaderParam("Authorization") String token,
                                           @QueryParam("size") int size,
                                           @QueryParam("cursor") String cursor) {
        LOG.fine("Attempt to fetch user help requests");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        return queryUnanswered(cursor, size);
    }


    /**
     * Queries help requests from the datastore.
     *
     * @param cursor The cursor for pagination.
     * @param size   The number of requests to fetch.
     * @return Response containing the queried help requests.
     */
    private Response queryRequests(String cursor, int size){
        Transaction txn = datastore.newTransaction();

        try {
            EntityQuery.Builder query = Query.newEntityQueryBuilder()
                    .setKind("Help")
                    //.setOrderBy(StructuredQuery.OrderBy.desc("submitted"))
                    .setLimit(size);

            if (!cursor.equals("EMPTY") && !cursor.equals("")){
                query.setStartCursor(Cursor.fromUrlSafe(cursor));
            }

            QueryResults<Entity> results = txn.run(query.build());

            List<Entity> requestList = new ArrayList<>();

            while (results.hasNext()) {
                Entity feedback = results.next();
                requestList.add(feedback);
            }

            QueryResponse response = new QueryResponse();
            response.setResults(requestList);
            response.setCursor(results.getCursorAfter().toUrlSafe());

            LOG.info( "Help requests fetched");
            txn.commit();
            Gson g = new Gson();
            return Response.ok(g.toJson(response))
                    //.header("X-Cursor",results.getCursorAfter().toUrlSafe())
                    .build();

        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


    /**
     * Queries unanswered help requests from the datastore.
     *
     * @param cursor The cursor for pagination.
     * @param size   The number of requests to fetch.
     * @return Response containing the queried unanswered help requests.
     */
    private Response queryUnanswered(String cursor, int size){
        Transaction txn = datastore.newTransaction();

        try {
            EntityQuery.Builder query = Query.newEntityQueryBuilder()
                    .setKind("Help")
                    .setFilter(StructuredQuery.PropertyFilter.eq("replied", ""))
                    .setLimit(size);

            if (!cursor.equals("EMPTY") && !cursor.equals("")){
                query.setStartCursor(Cursor.fromUrlSafe(cursor));
            }

            QueryResults<Entity> results = txn.run(query.build());

            List<Entity> requestList = new ArrayList<>();

            while (results.hasNext()) {
                Entity feedback = results.next();
                requestList.add(feedback);
            }

            QueryResponse response = new QueryResponse();
            response.setResults(requestList);
            response.setCursor(results.getCursorAfter().toUrlSafe());

            LOG.info( "Unanswered help requests fetched");
            txn.commit();

            Gson g = new Gson();
            return Response.ok(g.toJson(response)).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

}
