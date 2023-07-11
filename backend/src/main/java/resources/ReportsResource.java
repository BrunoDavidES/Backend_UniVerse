package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
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

/**
 * Resource class for handling reports.
 */
@Path("/reports")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ReportsResource {
    private static final Logger LOG = Logger.getLogger(ReportsResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    /**
     * Endpoint for posting a report.
     *
     * @param token The authorization token.
     * @param data  The report data.
     * @return A response indicating the success or failure of the operation.
     * It will return 401 error if the token doesn't exist.
     * It will return 400 error if there are missing or wrong parameters.
     */
    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postReports(@HeaderParam("Authorization") String token,  ReportData data){
        LOG.fine("Attempt to post report.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        if(!data.validate()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }
        return addReport(decodedToken, data);
    }


    /**
     * Endpoint for editing a report.
     *
     * @param token The authorization token.
     * @param id    The ID of the report to edit.
     * @return A response indicating the success or failure of the operation.
     * It will return 401 error if the toke doesn't exist.
     * It will return 400 error if the report doesn't exist
     * or if the user was not the one creating the report.
     */
    @POST
    @Path("/edit/{id}")
    public Response editReport(@HeaderParam("Authorization") String token, @PathParam("id") String id){
        LOG.fine("Attempt to edit report");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        return addNewInfoReport(decodedToken, id);
    }

    /**
     * Endpoint for changing the status of a report.
     *
     * @param token  The authorization token.
     * @param id     The ID of the report to update.
     * @param status The new status of the report.
     * @return A response indicating the success or failure of the operation.
     * It will return 401 if the token doesn't exist.
     * It will return 400 error if the report doesn't exist
     * or if the user role is different from backoffice or admin.
     */
    @POST
    @Path("/status/{id}/{status}")
    public Response statusReport(@HeaderParam("Authorization") String token, @PathParam("id") String id, @PathParam("status") String status){
        LOG.fine("Attempt to edit report");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        return changeStatusValidation(decodedToken, id, status);
    }

    /**
     * Endpoint for querying reports.
     *
     * @param token   The authorization token.
     * @param limit   The maximum number of results to return.
     * @param cursor  The cursor for pagination.
     * @param filters The filters to apply to the query.
     * @return A response containing the queried reports.
     * It will return 401 error if the token doesn't exist.
     * It will return 400 error if the user role is different from backoffice or admin.
     */
    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryReports(@HeaderParam("Authorization") String token, @QueryParam("limit") String limit,
                                 @QueryParam("offset") String cursor, Map<String, String> filters) {
        LOG.fine("Attempt to query reports.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        if(!getRole(decodedToken).equals(BO) && !getRole(decodedToken).equals(ADMIN) ){
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
        }
        return prepareQueryReportsFilter(filters, limit, cursor);
    }


    /**
     * Endpoint for querying unresolved reports.
     *
     * @param token   The authorization token.
     * @param limit   The maximum number of results to return.
     * @param cursor  The cursor for pagination.
     * @param filters The filters to apply to the query.
     * @return A response containing the queried unresolved reports.
     * It will return 401 error if the token doesn't exist.
     * It will return 400 error if the user role is different from backoffice or admin.
     */
    @POST
    @Path("/query/unresolved")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryUnresolvedReports(@HeaderParam("Authorization") String token, @QueryParam("limit") String limit,
                                 @QueryParam("offset") String cursor, Map<String, String> filters) {
        LOG.fine("Attempt to query reports.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        if(!getRole(decodedToken).equals(BO) && !getRole(decodedToken).equals(ADMIN)){
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();

        }
        return prepareUnresolvedReportsFilter(filters, limit, cursor);
    }

    /**
     * Endpoint for getting the number of unresolved reports.
     *
     * @param token The authorization token.
     * @return A response containing the number of unresolved reports.
     * It will return 401 error if the token doesn't exist.
     * It will return 400 error if the user role is different from backoffice or admin.
     */
    @GET
    @Path("/unresolved")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response numberOfUnresolvedReports(@HeaderParam("Authorization") String token) {
        LOG.fine("Trying to know how many unresolved reports exist");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        if(!getRole(decodedToken).equals(BO) && !getRole(decodedToken).equals(ADMIN)){
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();

        }
        return numUnresolvedReports();
    }

    /**
     * Adds a new report to the datastore.
     *
     * @param decodedToken The decoded authorization token.
     * @param data         The report data.
     * @return A response indicating the success or failure of the operation.
     */
    private Response addReport(FirebaseToken decodedToken, ReportData data){
        Transaction txn = datastore.newTransaction();
        try {
            Key reportKey;
            Entity entry;
            String id;
            do {
                id = UUID.randomUUID().toString();
                reportKey = datastore.newKeyFactory().setKind(REPORT).newKey(id);
                entry = txn.get(reportKey);
            } while(entry != null);

            Entity.Builder builder = Entity.newBuilder(reportKey);

            builder.set("title", data.getTitle())
                    .set("id", id)
                    .set("reporter", String.valueOf(decodedToken.getUid()))
                    .set("location", data.getLocation())
                    .set("status", STATUS_UNSEEN)
                    .set("time_creation", Timestamp.now());

            entry = builder.build();
            txn.add(entry);

            LOG.info("Report registered " + id);
            txn.commit();
            return Response.ok(id).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Adds new information to an existing report.
     *
     * @param decodedToken The decoded authorization token.
     * @param id           The ID of the report to update.
     * @return A response indicating the success or failure of the operation.
     */
    private Response addNewInfoReport(FirebaseToken decodedToken, String id){
        Transaction txn = datastore.newTransaction();
        try {
            Key eventKey = datastore.newKeyFactory().setKind(REPORT).newKey(id);
            Entity entry = txn.get(eventKey);

            if( entry == null ) {
                txn.rollback();
                LOG.warning(REPORT_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(REPORT_DOES_NOT_EXIST).build();
            } else if(!entry.getString("reporter").equals(decodedToken.getUid())) {
                txn.rollback();
                LOG.warning("Wrong author.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Wrong author.").build();
            }else {
                Entity.Builder builder = Entity.newBuilder(entry);

                builder.set("time_lastUpdated", Timestamp.now());

                Entity newEntry = builder.build();
                txn.update(newEntry);

                LOG.info( "Report registered id: " + id);
                txn.commit();
                return Response.ok(id).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Validates the status change request and updates the status of a report.
     *
     * @param decodedToken The decoded authorization token.
     * @param id           The ID of the report to update.
     * @param status       The new status of the report.
     * @return A response indicating the success or failure of the operation.
     */
    private Response changeStatusValidation(FirebaseToken decodedToken, String id, String status){
        Transaction txn = datastore.newTransaction();
        try {
            Key eventKey = datastore.newKeyFactory().setKind(REPORT).newKey(id);
            Entity entry = txn.get(eventKey);

            String role = getRole(decodedToken);

            if( entry == null ) {
                txn.rollback();
                LOG.warning(REPORT_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(REPORT_DOES_NOT_EXIST).build();
            } else if(!role.equals(BO) && !role.equals(ADMIN)){
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }else {
                return changeStatus(txn, entry, id, status);
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Updates the status of a report.
     *
     * @param txn   The transaction object.
     * @param entry The report entity.
     * @param id    The ID of the report to update.
     * @param status The new status of the report.
     * @return A response indicating the success or failure of the operation.
     */
    private Response changeStatus(Transaction txn, Entity entry, String id, String status){
        if(status == null || (!status.equals(STATUS_SEEN) && !status.equals(STATUS_RESOLVED))){
            txn.rollback();
            LOG.warning("Invalid status.");
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid status.").build();
        }
        Entity.Builder builder = Entity.newBuilder(entry);

        builder.set("time_lastUpdated", Timestamp.now());
        builder.set("status", status);

        Entity newEntry = builder.build();
        txn.update(newEntry);

        LOG.info( "Report status has been altered.");
        txn.commit();
        return Response.ok(id).build();
    }


    /**
     * Prepares the query for reports with filters and executes it.
     *
     * @param filters The filters to apply to the query.
     * @param limit   The maximum number of results to return.
     * @param cursor  The cursor for pagination.
     * @return A response containing the queried reports.
     */
    private Response prepareQueryReportsFilter(Map<String, String> filters, String limit, String cursor){
        StructuredQuery.CompositeFilter attributeFilter = null;
        if( filters == null){
            filters = new HashMap<>(1);
        }
        StructuredQuery.PropertyFilter propFilter;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            propFilter = StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue());

            if(attributeFilter == null) {
                attributeFilter = StructuredQuery.CompositeFilter.and(propFilter);
            } else {
                attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);
            }
        }
        return queryReports(attributeFilter, limit, cursor);
    }

    /**
     * Executes the query for reports.
     *
     * @param attributeFilter The filter to apply to the query.
     * @param limit           The maximum number of results to return.
     * @param cursor          The cursor for pagination.
     * @return A response containing the queried reports.
     */
    private Response queryReports(StructuredQuery.CompositeFilter attributeFilter, String limit, String cursor){
        QueryResults<Entity> queryResults;
        EntityQuery.Builder query = Query.newEntityQueryBuilder()
                .setKind(REPORT)
                .setFilter(attributeFilter)
                .setLimit(Integer.parseInt(limit));

        if ( !cursor.equals("EMPTY") ){
            query.setStartCursor(Cursor.fromUrlSafe(cursor));
        }

        queryResults = datastore.run(query.build());

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        QueryResponse response = new QueryResponse();
        response.setResults(results);
        response.setCursor(queryResults.getCursorAfter().toUrlSafe());

        LOG.info("Query de reports pedido");
        Gson g = new Gson();

        return Response.ok(g.toJson(response))
                //.header("X-Cursor",queryResults.getCursorAfter().toUrlSafe())
                .build();
    }


    /**
     * Prepares the query for unresolved reports with filters and executes it.
     *
     * @param filters The filters to apply to the query.
     * @param limit   The maximum number of results to return.
     * @param cursor  The cursor for pagination.
     * @return A response containing the queried unresolved reports.
     */
    private Response prepareUnresolvedReportsFilter(Map<String, String> filters, String limit, String cursor){
        StructuredQuery.CompositeFilter attributeFilter =
                StructuredQuery.CompositeFilter.and( StructuredQuery.PropertyFilter.neq( STATUS_CLAIM, STATUS_RESOLVED ) );
        if( filters == null){
            filters = new HashMap<>(1);
        }

        StructuredQuery.PropertyFilter propFilter;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            propFilter = StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue());

            attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);

        }
        return unresolvedReports(attributeFilter, limit, cursor);
    }

    /**
     * Executes the query for unresolved reports.
     *
     * @param attributeFilter The filter to apply to the query.
     * @param limit           The maximum number of results to return.
     * @param cursor          The cursor for pagination.
     * @return A response containing the queried unresolved reports.
     */
    private Response unresolvedReports(StructuredQuery.CompositeFilter attributeFilter, String limit, String cursor){
        QueryResults<Entity> queryResults;
        EntityQuery.Builder query = Query.newEntityQueryBuilder()
                .setKind(REPORT)
                .setFilter(attributeFilter)
                .setLimit(Integer.parseInt(limit));

        if ( !cursor.equals("EMPTY") ){
            query.setStartCursor(Cursor.fromUrlSafe(cursor));
        }

        queryResults = datastore.run(query.build());

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        QueryResponse response = new QueryResponse();
        response.setResults(results);
        response.setCursor(queryResults.getCursorAfter().toUrlSafe());

        LOG.info("Query de reports não resolvidos pedido");
        Gson g = new Gson();

        return Response.ok(g.toJson(response)).build();
    }

    /**
     * Retrieves the number of unresolved reports.
     *
     * @return A response containing the number of unresolved reports.
     */
    private Response numUnresolvedReports(){
        Query<Key> query = Query.newKeyQueryBuilder()
                .setKind(REPORT)
                .setFilter(StructuredQuery.PropertyFilter.neq("status", STATUS_RESOLVED))
                .build();

        QueryResults<Key> queryResults = datastore.run(query);

        int counter = 0;

        while (queryResults.hasNext()){
            counter++;
            queryResults.next();
        }

        return Response.ok(counter).build();
    }
}
