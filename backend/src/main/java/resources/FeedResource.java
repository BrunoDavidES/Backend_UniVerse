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

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedResource {
    private static final Logger LOG = Logger.getLogger(FeedResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


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

            Transaction txn = datastore.newTransaction();
            try {
                if (kind.equals(EVENT)) {
                    Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.getDepartment());
                    Entity department = txn.get(departmentKey);
                    if( department == null ) {
                        txn.rollback();
                        LOG.warning(WRONG_DEPARTMENT);
                        return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
                    }
                    if (role.equals(TEACHER)) {
                        Key teacherKey = datastore.newKeyFactory().setKind(USER).newKey(username);
                        Entity teacher = txn.get(teacherKey);

                        if (!teacher.getString("department").equals(data.getDepartment())) {
                            txn.rollback();
                            LOG.warning("No permission to post an Event in that department");
                            return Response.status(Response.Status.FORBIDDEN).entity("No permission to create an event in that department.").build();
                        }
                    } else if (role.equals(STUDENT)) {
                        Key studentKey = datastore.newKeyFactory().setKind(USER).newKey(username);
                        Entity student = txn.get(studentKey);

                        if (!student.getString("nucleus").equals(data.getNucleus()) || !student.getString("nucleus_job").equals("President")) {
                            txn.rollback();
                            LOG.warning("No permission to post an Event in that nucleus");
                            return Response.status(Response.Status.FORBIDDEN).entity("No permission to post an Event in that nucleus").build();
                        }
                    }
                }

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
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        } catch (InvalidParameterException e) {
            LOG.warning("Token is invalid: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Token is invalid").build();
        }
    }

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
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

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
}
