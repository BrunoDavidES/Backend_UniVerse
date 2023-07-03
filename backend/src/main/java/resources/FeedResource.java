package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import util.FeedData;

import com.google.gson.Gson;
import util.ValToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Logger;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedResource {

    private static final String CAPI = "Your not one of us\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⢀⣞⣆⢀⣠⢶⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
            "⠀⢀⣀⡤⠤⠖⠒⠋⠉⣉⠉⠹⢫⠾⣄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
            "⢠⡏⢰⡴⠀⠀⠀⠉⠙⠟⠃⠀⠀⠀⠈⠙⠦⣄⡀⢀⣀⣠⡤⠤⠶⠒⠒⢿⠋⠈⠀⣒⡒⠲⠤⣄⡀⠀⠀⠀⠀⠀⠀\n" +
            "⢸⠀⢸⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠀⠴⠂⣀⠀⠀⣴⡄⠉⢷⡄⠚⠀⢤⣒⠦⠉⠳⣄⡀⠀⠀⠀\n" +
            "⠸⡄⠼⠦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣄⡂⠠⣀⠐⠍⠂⠙⣆⠀⠀\n" +
            "⠀⠙⠦⢄⣀⣀⣀⣀⡀⠀⢷⠀⢦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠰⡇⠠⣀⠱⠘⣧⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠈⠉⢷⣧⡄⢼⠀⢀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠀⡈⠀⢄⢸⡄\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⣿⡀⠃⠘⠂⠲⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⠀⡈⢘⡇\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢫⡑⠣⠰⠀⢁⢀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠁⣸⠁\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⣯⠂⡀⢨⠀⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡆⣾⡄⠀⠀⠀⠀⣀⠐⠁⡴⠁⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⣧⡈⡀⢠⣧⣤⣀⣀⡀⢀⡀⠀⠀⢀⣼⣀⠉⡟⠀⢀⡀⠘⢓⣤⡞⠁⠀⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢺⡁⢁⣸⡏⠀⠀⠀⠀⠁⠀⠉⠉⠁⠹⡟⢢⢱⠀⢸⣷⠶⠻⡇⠀⠀⠀⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢈⡏⠈⡟⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠑⢄⠁⠀⠻⣧⠀⠀⣹⠁⠀⠀⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣀⡤⠚⠃⣰⣥⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣾⠼⢙⡷⡻⠀⡼⠁⠀⠀⠀⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠟⠿⡿⠕⠊⠉⠀⠀⠀⠀⠀⠀⠀⠀⣠⣴⣶⣾⠉⣹⣷⣟⣚⣁⡼⠁⠀⠀⠀⠀⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠙⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";

    private static final String MISSING_OR_WRONG_PARAMETER = "Missing or wrong parameter.";
    private static final String TOKEN_NOT_FOUND = "Token not found.";
    private static final String BO = "BO";
    private static final String TEACHER = "T";
    private static final String WORKER = "W";
    private static final String STUDENT = "S";
    private static final String ADMIN = "A";
    private static final String ROLE = "role";
    private static final String USER = "User";
    private static final String EVENT = "Event";
    private static final String NEWS = "News";
    private static final String USER_CLAIM = "user";
    private static final String NAME_CLAIM = "name";
    private static final String NICE_TRY = "Nice try but your not a capi person.";
    private static final String PERMISSION_DENIED = "Permission denied.";
    private static final String DEPARTMENT = "Department";
    private static final String WRONG_PRESIDENT = "President doesn't exists.";
    private static final String DEPARTMENT_ALREADY_EXISTS = "Department already exists.";
    private static final String WRONG_DEPARTMENT = "Department does not exist.";
    private static final String WRONG_MEMBER = "Member doesn't exists.";
    private static final Logger LOG = Logger.getLogger(FeedResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    @POST
    @Path("/post/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postEntry(@Context HttpServletRequest request, @PathParam("kind") String kind, FeedData data){
        LOG.fine("Attempt to post entry to feed.");

        if((!kind.equals(NEWS) && !kind.equals(EVENT)) || !data.validate(kind)) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            String role = String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "");
            String name = String.valueOf(token.getClaim(NAME_CLAIM)).replaceAll("\"", "");
            String username = String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", "");


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

            Transaction txn = datastore.newTransaction();
            try {
                if (kind.equals(EVENT)) {
                    if (role.equals(TEACHER)) {
                        Key teacherKey = datastore.newKeyFactory().setKind(USER).newKey(username);
                        Entity teacher = txn.get(teacherKey);

                        if (!teacher.getString("department").equals(data.department)) {
                            txn.rollback();
                            LOG.warning("No permission to post an Event in that department");
                            return Response.status(Response.Status.FORBIDDEN).entity("No permission to create an event in that department.").build();
                        }
                    } else if (role.equals(STUDENT)) {
                        Key studentKey = datastore.newKeyFactory().setKind(USER).newKey(username);
                        Entity student = txn.get(studentKey);

                        if (!student.getString("nucleus").equals(data.nucleus) || !student.getString("nucleus_job").equals("President")) {
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
                    id = UUID.randomUUID().toString();
                    feedKey = datastore.newKeyFactory().setKind(kind).newKey(id);
                    entry = txn.get(feedKey);
                } while (entry != null);

                Entity.Builder builder = Entity.newBuilder(feedKey);
                if (kind.equals(EVENT)) { //construtor de eventos

                    builder.set("id", id)
                            .set("title", data.title)
                            .set("authorName", name)
                            .set("authorUsername", username)
                            .set("startDate", data.startDate)
                            .set("endDate", data.endDate)
                            .set("location", data.location)
                            .set("department", data.department)
                            .set("isPublic", data.isPublic)
                            .set("capacity", data.capacity)
                            .set("isItPaid", data.isItPaid)
                            .set("validated_backoffice", "false")
                            .set("time_creation", Timestamp.now());

                }else {
                    //construtor de news
                    // Caso se vá buscar uma notícia de outro site, por parte do backoffice,
                    // e se queira por o author como "Jornal Expresso", por exemplo
                    if (role.equals(BO) && data.authorNameByBO != null && !data.authorNameByBO.equals("")){
                        name = data.authorNameByBO;
                    }
                    builder.set("id", id)
                            .set("title", data.title)
                            .set("authorName", name)
                            .set("authorUsername", username)
                            .set("validated_backoffice", "false")
                            .set("time_creation", Timestamp.now());

                }
                entry = builder.build();

                txn.add(entry);
                LOG.info(kind + " posted " + data.title + "; id: " + id);
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
    public Response editEntry(@Context HttpServletRequest request, @PathParam("kind") String kind, @PathParam("id") String id, FeedData data){
        LOG.fine("Attempt to edit feed entry.");

        if(!kind.equals(NEWS) && !kind.equals(EVENT)) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                txn.rollback();
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }


            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            String role = String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "");
            String username = String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", "");

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
            }
            else {

                Entity.Builder newEntry = Entity.newBuilder(entry);
                if (kind.equals(EVENT)) { //construtor de eventos
                    newEntry.set("title", data.title)
                            .set("startDate", data.startDate)
                            .set("endDate", data.endDate)
                            .set("location", data.location)
                            .set("isPublic", data.isPublic)
                            .set("capacity", data.capacity)
                            .set("isItPaid", data.isItPaid)
                            .set("validated_backoffice", data.validated_backoffice)
                            .set("time_lastupdated", Timestamp.now());
                }else { //construtor de news
                    newEntry.set("title", data.title)
                            .set("time_lastupdated", Timestamp.now());

                }
                Entity updatedEntryEntry = newEntry.build();
                txn.update(updatedEntryEntry);

                LOG.info(kind + " edited " + data.title + "; id: " + id);
                txn.commit();
                return Response.ok().build();
            }
        }
        finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/delete/{kind}/{id}")
    public Response deleteEntry(@Context HttpServletRequest request, @PathParam("kind") String kind, @PathParam("id") String id){
        LOG.fine("Attempt to delete event.");

        if((!kind.equals(NEWS) && !kind.equals(EVENT))) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                txn.rollback();
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            String role = String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "");
            String username = String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", "");

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
    public Response queryEntries(@Context HttpServletRequest request, @PathParam("kind") String kind,
                                @QueryParam("limit") String limit,
                                @QueryParam("offset") String cursor, Map<String, String> filters){
        LOG.fine("Attempt to query feed " + kind);

        //Verificar, caso for evento privado, se o token é valido
        final ValToken validator = new ValToken();
        DecodedJWT token = validator.checkToken(request);

        if(kind.equals(EVENT)) {
            if (token == null) {
                LOG.info(TOKEN_NOT_FOUND);
                if (filters == null)
                    filters = new HashMap<>(2);
                filters.put("isPublic", "yes");
            }
        }

        if (token != null){
            String role = String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "");

            if( !(role.equals(BO) || role.equals(ADMIN)) ) {
                filters.put("validated_backoffice", "true");
            }
        }
        else {
            filters.put("validated_backoffice", "true");
        }

        QueryResults<Entity> queryResults;

        StructuredQuery.CompositeFilter attributeFilter = null;
        if( filters == null ){
            filters = new HashMap<>(1);
        }
        StructuredQuery.PropertyFilter propFilter;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            propFilter = StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue());

            if(attributeFilter == null)
                attributeFilter = StructuredQuery.CompositeFilter.and(propFilter);
            else
                attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);
        }

        EntityQuery.Builder query = Query.newEntityQueryBuilder() //tá feio mas só funciona assim, raios da datastore
                .setKind(kind)
                .setFilter(attributeFilter)
                .setLimit(Integer.parseInt(limit));

        if ( !cursor.equals("EMPTY") ){
            query.setStartCursor(Cursor.fromUrlSafe(cursor));
        }

        queryResults = datastore.run(query.build());

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        LOG.info("Query de " + kind + " pedido");
        Gson g = new Gson();
        return Response.ok(g.toJson(results))
                .header("X-Cursor",queryResults.getCursorAfter().toUrlSafe())
                .build();

    }

    @POST
    @Path("/numberOf/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryEntriesNum(@Context HttpServletRequest request, @PathParam("kind") String kind, Map<String, String> filters) {
        LOG.fine("Attempt to count the query feed " + kind);

        // Verificar, caso for evento privado, se o token é valido
        final ValToken validator = new ValToken();
        DecodedJWT token = validator.checkToken(request);

        if (kind.equals(EVENT)) {
            if (token == null) {
                LOG.info(TOKEN_NOT_FOUND);
                if (filters == null)
                    filters = new HashMap<>(1);
                filters.put("isPublic", "yes");
            }
        }

        if (token != null){
            String role = String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "");

            if( !(role.equals(BO) || role.equals(ADMIN)) ) {
                filters.put("validated_backoffice", "true");
            }
        }
        else {
            filters.put("validated_backoffice", "true");
        }

        QueryResults<Entity> queryResults;

        StructuredQuery.CompositeFilter attributeFilter = null;
        if (filters == null) {
            filters = new HashMap<>(1);
        }
        StructuredQuery.PropertyFilter propFilter;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            propFilter = StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue());

            if (attributeFilter == null)
                attributeFilter = StructuredQuery.CompositeFilter.and(propFilter);
            else
                attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);
        }

        Query<Entity> query = Query.newEntityQueryBuilder()
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
    public Response queryEntriesTimeGap(@Context HttpServletRequest request, @PathParam("kind") String kind, @PathParam("firstDate") String firstDate,  @PathParam("endDate") String endDate){
        LOG.fine("Attempt to query feed ");

        //VERIFICAR QUE O UTILIZADOR QUE CHAMA ESTE METODO É BACOFFICE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        final ValToken validator = new ValToken();
        DecodedJWT token = validator.checkToken(request);

        if (token == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        String role = String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "");
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
