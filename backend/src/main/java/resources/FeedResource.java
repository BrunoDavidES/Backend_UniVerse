package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import models.FeedData;

import com.google.gson.Gson;
import utils.QueryResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.FirebaseAuth.authenticateToken;
import static utils.FirebaseAuth.getRole;

/**
 * Resource class for handling feed-related operations.
 */
@Path("/feed")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedResource {
    private static final Logger LOG = Logger.getLogger(FeedResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    /**
     * Endpoint for posting an entry to the feed.
     *
     * @param token Authorization token
     * @param kind  Feed entry kind (NEWS or EVENT)
     * @param data  Feed data to be posted
     * @return Response indicating the success or failure of the operation.
     * It will return 401 error if there is no token.
     *
     * It will return 403 error if user trying to post event has a role different from backoffice/admin/teacher/student,
     * if the user trying to add news has a role different fom backoffice or admin,
     * if a teacher tries to add an event from a department different from his own
     * or if a student tries to add an event for a nucleus he is not president of.
     *
     * It will return 400 error if there are any missing or wring parameters,
     * if event start date is in the past,
     * if event start date is after event end date,
     * if date format is invalid
     * or if department given does not exist.
     */
    @POST
    @Path("/post/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postEntry(@HeaderParam("Authorization") String token, @PathParam("kind") String kind, FeedData data){
        LOG.fine("Attempt to post entry to feed.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        if((!kind.equals(NEWS) && !kind.equals(EVENT)) || !data.validate(kind)) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        return postFeedValidation(decodedToken, kind, data);
    }


    /**
     * Endpoint for editing a feed entry.
     *
     * @param token Authorization token
     * @param kind  Feed entry kind (NEWS or EVENT)
     * @param id    ID of the entry to be edited
     * @param data  Updated feed data
     * @return Response indicating the success or failure of the operation.
     * It will return 401 error if there is no token.
     * It will return 400 error if there are any missing or wring parameters,
     * if the feed to alter doesn't exist
     * or if the data passed to edit the feed is invalid.
     * It will return 403 error if the user role is different from backoffice or admin.
     */
    @PATCH
    @Path("/edit/{kind}/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editEntry(@HeaderParam("Authorization") String token, @PathParam("kind") String kind, @PathParam("id") String id, FeedData data){
        LOG.fine("Attempt to edit feed entry.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        if(!kind.equals(NEWS) && !kind.equals(EVENT)) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        return editFeedValidation(decodedToken, data, kind, id);
    }

    /**
     * Endpoint for deleting a feed entry.
     *
     * @param token Authorization token
     * @param kind  Feed entry kind (NEWS or EVENT)
     * @param id    ID of the entry to be deleted
     * @return Response indicating the success or failure of the operation
     * It will return 401 error if there is no token.
     * It will return 400 error if there are any missing or wring parameters or if the feed to alter doesn't exist.
     * It will return 403 error if the user role is different from backoffice, admin or if the user is not the author of this feed.
     */
    @DELETE
    @Path("/delete/{kind}/{id}")
    public Response deleteEntry(@HeaderParam("Authorization") String token, @PathParam("kind") String kind, @PathParam("id") String id){
        LOG.fine("Attempt to delete event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        if((!kind.equals(NEWS) && !kind.equals(EVENT))) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }
        return deleteFeed(decodedToken, kind, id);
    }


    /**
     * Endpoint for querying feed entries.
     *
     * @param token   Authorization token
     * @param kind    Feed entry kind (NEWS or EVENT)
     * @param limit   Limit on the number of entries to retrieve
     * @param cursor  Offset cursor for paginated results
     * @param filters Map of filters to apply to the query
     * @return Response containing the query results
     */
    @POST
    @Path("/query/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryEntries(@HeaderParam("Authorization") String token, @PathParam("kind") String kind,
                                 @QueryParam("limit") String limit,
                                 @QueryParam("offset") String cursor, Map<String, String> filters){
        LOG.fine("Attempt to query feed " + kind);

        FirebaseToken decodedToken = authenticateToken(token);

        if(kind.equals(EVENT)) {
            if (decodedToken == null) {
                LOG.info(TOKEN_NOT_FOUND);
                if (filters == null)
                    filters = new HashMap<>(2);
                filters.put("isPublic", "yes");
            }
        }

        if( filters == null ){
            filters = new HashMap<>(1);
        }

        if (decodedToken != null){
            String role = getRole(decodedToken);

            if( !(role.equals(BO) || role.equals(ADMIN)) ) {
                filters.put("validated_backoffice", "true");
            }
        }
        else {
            filters.put("validated_backoffice", "true");
        }

        EntityQuery.Builder query = Query.newEntityQueryBuilder().setKind(kind).setLimit(Integer.parseInt(limit));

        QueryResults<Entity> queryResults;

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            query.setFilter(StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue()));
        }


        if ( !cursor.equals("EMPTY") ){
            query.setStartCursor(Cursor.fromUrlSafe(cursor));
        }

        queryResults = datastore.run(query.build());

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        QueryResponse response = new QueryResponse();
        response.setResults(results);
        response.setCursor(queryResults.getCursorAfter().toUrlSafe());

        LOG.info("Query de " + kind + " pedido");
        Gson g = new Gson();
        return Response.ok(g.toJson(response)).build();

    }


    /**
     * Endpoint for querying the number of feed entries.
     *
     * @param token   Authorization token
     * @param kind    Feed entry kind (NEWS or EVENT)
     * @param filters Map of filters to apply to the query
     * @return Response containing the count of entries
     */
    @POST
    @Path("/numberOf/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryEntriesNum(@HeaderParam("Authorization") String token, @PathParam("kind") String kind, Map<String, String> filters) {
        LOG.fine("Attempt to count the query feed " + kind);

        if (!kind.equals(EVENT) && !kind.equals(NEWS)){
            LOG.warning("Kind " + kind + " is not valid.");
            return Response.status(Response.Status.BAD_REQUEST).entity("Kind " + kind + " não é válido.").build();
        }

        FirebaseToken decodedToken = authenticateToken(token);

        if (kind.equals(EVENT)) {
            if (decodedToken == null) {
                LOG.info(TOKEN_NOT_FOUND);
                if (filters == null)
                    filters = new HashMap<>(1);
                filters.put("isPublic", "yes");
            }
        }

        if (filters == null) {
            filters = new HashMap<>(1);
        }

        if (decodedToken != null){
            String role = getRole(decodedToken);

            if( !(role.equals(BO) || role.equals(ADMIN)) ) {
                filters.put("validated_backoffice", "true");
            }
        }
        else {
            filters.put("validated_backoffice", "true");
        }

        //QueryResults<Entity> queryResults;
        QueryResults<Key> queryResults;

        StructuredQuery.CompositeFilter attributeFilter = null;

        StructuredQuery.PropertyFilter propFilter;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            propFilter = StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue());

            if (attributeFilter == null)
                attributeFilter = StructuredQuery.CompositeFilter.and(propFilter);
            else
                attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);
        }

        //Query<Entity> query = Query.newEntityQueryBuilder()
        Query<Key> query = Query.newKeyQueryBuilder()
                .setKind(kind)
                .setFilter(attributeFilter)
                .build();

        queryResults = datastore.run(query);

        LOG.info("Received a query!");
        int count = 0;
        // Get the total number of entities
        while (queryResults.hasNext()) {
            queryResults.next();
            count++;
        }

        return Response.ok(count).build();
    }


    /**
     * Endpoint for querying feed entries within a specified time gap.
     *
     * @param token     Authorization token
     * @param kind      Feed entry kind (NEWS or EVENT)
     * @param firstDate Start date of the time gap (inclusive)
     * @param endDate   End date of the time gap (inclusive)
     * @return Response containing the query results within the specified time gap
     */
    @POST
    @Path("/query/{kind}/timeGap/{firstDate}/{endDate}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryEntriesTimeGap(@HeaderParam("Authorization") String token, @PathParam("kind") String kind, @PathParam("firstDate") String firstDate,  @PathParam("endDate") String endDate){
        LOG.fine("Attempt to query feed ");

        //VERIFICAR QUE O UTILIZADOR QUE CHAMA ESTE METODO É BACOFFICE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
        StructuredQuery.CompositeFilter attributeFilter;
        QueryResults<Entity> queryResults;

        Timestamp firstDateTS = Timestamp.parseTimestamp(firstDate);
        Timestamp endDateTS = Timestamp.parseTimestamp(endDate);

        attributeFilter = StructuredQuery.CompositeFilter.and(StructuredQuery.PropertyFilter.ge("time_creation", firstDateTS),
                StructuredQuery.PropertyFilter.le("time_creation", endDateTS));

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(attributeFilter)
                .build();

        queryResults = datastore.run(query);

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        LOG.info("Query de " + kind + " entre um intervalo de datas foi pedido");
        Gson g = new Gson();
        return Response.ok(g.toJson(results)).build();
    }

    // Helper methods

    private Response postFeedValidation(FirebaseToken decodedToken, String kind, FeedData data){
        try {
            String role = getRole(decodedToken);
            String name = decodedToken.getName();
            String username = decodedToken.getUid();


            // Para já, só docentes é q fazem eventos
            // No futuro, pôr presidente da AE e possivelmente dos Nucleos
            if (kind.equals(EVENT) && !role.equals(TEACHER) && !role.equals(STUDENT) && !role.equals(BO) && !role.equals(ADMIN)){
                LOG.warning("No permission to create an event.");
                return Response.status(Response.Status.FORBIDDEN).entity("No permission to create an event.").build();
            }

            if (kind.equals(NEWS) && !role.equals(BO) && !role.equals(ADMIN)){
                LOG.warning("No permission to post news.");
                return Response.status(Response.Status.FORBIDDEN).entity("No permission to create an event.").build();
            }

            if (kind.equals(EVENT)) {
                try {

                    String[] temp1 = data.getStartDate().split("-");
                    Date today = Calendar.getInstance().getTime();
                    Calendar c1 = Calendar.getInstance();
                    c1.set(Integer.parseInt(temp1[2]), Integer.parseInt(temp1[1]) -1, Integer.parseInt(temp1[0]));

                    Date startDate = c1.getTime();

                    if (today.after(startDate)) {
                        LOG.warning("Start date is older than today's date");
                        return Response.status(Response.Status.BAD_REQUEST).entity("Start date is older than today's date.").build();
                    }

                    if (!data.getEndDate().equals(("").trim())) {
                        String[] temp2 = data.getEndDate().split("-");
                        Calendar c2 = Calendar.getInstance();
                        c2.set(Integer.parseInt(temp2[2]), Integer.parseInt(temp2[1]) -1, Integer.parseInt(temp2[0]));

                        Date endDate = c2.getTime();

                        if (startDate.after(endDate)) {
                            LOG.warning("Start date is older than event's end date");
                            return Response.status(Response.Status.BAD_REQUEST).entity("Start date is older than event's end date.").build();
                        }
                    }
                } catch (Exception e) {
                    LOG.warning("Invalid format for Date");
                    return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Date. Try using the format dd-mm-yyyy with a valid Date." +
                            "\nValid Dates are today's date and all the following.").build();
                }
            }

            return postFeedTxnValidation( data, kind, role, username, name);
        } catch (InvalidParameterException e) {
            LOG.warning("Token is invalid: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Token is invalid").build();
        }
    }

    private Response postFeedTxnValidation(FeedData data, String kind,  String role, String username, String name){
        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(username);
            Entity user = txn.get(userKey);

            if (kind.equals(EVENT)) {

                if (role.equals(TEACHER)) {

                    Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(user.getString("department"));
                    Entity department = txn.get(departmentKey);
                    if( department == null ) {
                        txn.rollback();
                        LOG.warning("User with role Teacher is not in a department.");
                        return Response.status(Response.Status.FORBIDDEN).entity("User with role Teacher is not in a department.").build();
                    }

                } else if (role.equals(BO) || role.equals(ADMIN)) {
                    String departmentID = data.getDepartment()!= null ? data.getDepartment() : user.getString("department");

                    Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(departmentID);
                    Entity department = txn.get(departmentKey);
                    if( department == null ) {
                        txn.rollback();
                        LOG.warning(WRONG_DEPARTMENT);
                        return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
                    }

                } else{

                    Key nucleusKey = datastore.newKeyFactory().setKind(NUCLEUS).newKey(user.getString("nucleus"));
                    Entity nucleus = txn.get(nucleusKey);

                    if (nucleus == null || !nucleus.getString("president").equals(username)) {
                        txn.rollback();
                        LOG.warning("No permission to post an Event in that nucleus");
                        return Response.status(Response.Status.FORBIDDEN).entity("No permission to post an Event in that nucleus").build();
                    }
                }
            }
            return postFeed(txn, data, kind, username, name);
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    private Response postFeed(Transaction txn, FeedData data, String kind,  String username, String name){
        Key feedKey;
        Entity entry;
        String id;
        do {
            if (kind.equals(NEWS))
                id = (Long.MAX_VALUE - Instant.now().getEpochSecond())+ UUID.randomUUID().toString();
            else {
                String[] temp = data.getStartDate().split("-");
                Calendar c = Calendar.getInstance();
                c.set(Integer.parseInt(temp[2]), Integer.parseInt(temp[1]) -1, Integer.parseInt(temp[0]));
                //Sem o MAX_VALUE, listava primeiro os mais antigos
                id = (Long.MAX_VALUE - c.getTimeInMillis()) + UUID.randomUUID().toString();
            }

            feedKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            entry = txn.get(feedKey);
        } while (entry != null);

        Entity.Builder builder = Entity.newBuilder(feedKey);
        if (kind.equals(EVENT)) { //construtor de eventos

            builder.set("id", id)
                    .set("title", data.getTitle())
                    .set("authorName", name)
                    .set("authorUsername", username)
                    .set("startDate", data.getStartDate())
                    .set("endDate", data.getEndDate())
                    .set("location", data.getLocation())
                    .set("department", data.getDepartment())
                    .set("nucleus", data.getNucleus())
                    .set("isPublic", data.getIsPublic())
                    .set("capacity", data.getCapacity())
                    .set("isItPaid", data.getIsItPaid())
                    .set("validated_backoffice", "false")
                    .set("time_creation", Timestamp.now());

        }else {
            //construtor de news
            // Caso se vá buscar uma notícia de outro site, por parte do backoffice,
            // e se queira por o author como "Jornal Expresso", por exemplo
            if (data.authorNameByBO != null && !data.authorNameByBO.equals("")){
                name = data.authorNameByBO;
            }
            builder.set("id", id)
                    .set("title", data.getTitle())
                    .set("authorName", name)
                    .set("authorUsername", username)
                    .set("validated_backoffice", "false")
                    .set("time_creation", Timestamp.now());

        }
        entry = builder.build();

        txn.add(entry);
        LOG.info(kind + " posted " + data.getTitle() + "; id: " + id);
        txn.commit();
        return Response.ok(id).build();
    }


    private Response editFeedValidation(FirebaseToken decodedToken, FeedData data, String kind, String id){

        Transaction txn = datastore.newTransaction();

        try {
            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            String role = getRole(decodedToken);
            String username = decodedToken.getUid();

            if( entry == null ) {
                txn.rollback();
                LOG.warning(kind + " does not exist " + id);
                return Response.status(Response.Status.BAD_REQUEST).entity(kind + " does not exist " + id).build();
            } else if (!data.validateEdit(entry, kind)) {
                txn.rollback();
                LOG.warning("Invalid request for editEntry");
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request").build();
            }

            if( !(entry.getString("authorUsername").equals(username) || role.equals(BO) || role.equals(ADMIN)) ){
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
            }else {
                return editFeed(txn, entry, kind, id, data);
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
    private Response editFeed(Transaction txn, Entity entry, String kind, String id, FeedData data){
        Entity.Builder newEntry = Entity.newBuilder(entry);
        if (kind.equals(EVENT)) { //construtor de eventos
            newEntry.set("title", data.getTitle())
                    .set("startDate", data.getStartDate())
                    .set("endDate", data.getEndDate())
                    .set("location", data.getLocation())
                    .set("isPublic", data.getIsPublic())
                    .set("capacity", data.getCapacity())
                    .set("isItPaid", data.getIsItPaid())
                    .set("validated_backoffice", data.getValidated_backoffice())
                    .set("time_lastupdated", Timestamp.now());
        }else { //construtor de news
            newEntry.set("title", data.getTitle())
                    .set("validated_backoffice", data.getValidated_backoffice())
                    .set("time_lastupdated", Timestamp.now());

        }
        Entity updatedEntryEntry = newEntry.build();
        txn.update(updatedEntryEntry);

        LOG.info(kind + " edited " + data.getTitle() + "; id: " + id);
        txn.commit();
        return Response.ok().build();
    }

    private Response deleteFeed(FirebaseToken decodedToken, String kind, String id){

        Transaction txn = datastore.newTransaction();

        try {
            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            String role = getRole(decodedToken);
            String username = decodedToken.getUid();

            if( entry == null ) {
                txn.rollback();
                LOG.warning(kind + " does not exist");
                return Response.status(Response.Status.BAD_REQUEST).entity(kind + " does not exist").build();
            } else if( !(entry.getString("authorUsername").equals(username) || role.equals(BO) || role.equals(ADMIN)) ) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
            }else {
                txn.delete(eventKey);
                LOG.info(kind + " deleted " + id);
                txn.commit();
                return Response.ok(entry).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}
