package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import models.PersonalEventsData;
import models.ProfileData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.FirebaseAuth.authenticateToken;
import static utils.FirebaseAuth.getRole;

@Path("/profile")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ProfileResource {
    private static final Logger LOG = Logger.getLogger(ProfileResource.class.getName());

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public Gson g = new Gson();

    // Talvez adicionar LinkedIn

    @GET
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getProfile(@HeaderParam("Authorization") String token, @PathParam("username") String username){
        LOG.fine("Attempt to get profile by " + username);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        if(username == null){
            LOG.warning("username field is null");
            return Response.status(Response.Status.BAD_REQUEST).entity("Empty param").build();
        }

        String requester = decodedToken.getUid();
        Key userKey = datastore.newKeyFactory().setKind(USER).newKey(requester);
        Entity user = datastore.get(userKey);

        if( user == null ) {
            LOG.warning(USER_DOES_NOT_EXIST);
            return Response.status(Response.Status.BAD_REQUEST).entity("User does not exist "  + requester).build();
        }

        ProfileData data = new ProfileData();

        // Vai ter de mudar quando se souber os atributos a devolver em cada caso
        if (!username.equals(requester)){
            // Faz um perfil menos completo
            userKey = datastore.newKeyFactory().setKind(USER).newKey(username);
            user = datastore.get(userKey);

            if( user == null ) {
                LOG.warning(USER_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_DOES_NOT_EXIST).build();
            }
        }

        data.username = username;
        data.name = user.getString("name");
        data.email = user.getString("email");
        data.role = user.getString("role");
        data.department = user.getString("department");
        data.department_job = user.getString("department_job");

        if (data.role.equals(STUDENT)){
            data.nucleus = user.getString("nucleus");
            data.nucleus_job = user.getString("nucleus_job");
        }else {
            data.nucleus = "";
            data.nucleus_job = "";
        }

        if (data.role.equals(TEACHER)){
            data.office = user.getString("office");
        }else
            data.office = "";

        String requesterUsername = decodedToken.getUid();
        String requesterRole = getRole(decodedToken);

        if ( requesterUsername.equals(username) || requesterRole.equals(BO) || requesterRole.equals(ADMIN) ){
            data.license_plate = user.getString("license_plate");
            data.status = user.getString("status");
        }else {
            data.license_plate = "";
            data.status = "";
        }


        LOG.fine("Profile successfully gotten");
        return Response.ok(g.toJson(data)).build();
    }

    @POST
    @Path("/personalEvent/add")
    @Consumes(MediaType.APPLICATION_JSON)                                        //list composta por string que tem valor: "#papel-username"
    public Response addPersonalEvent(@HeaderParam("Authorization") String token, PersonalEventsData data) {
        LOG.fine("Attempt to add a personal event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
            Entity user = txn.get(userKey);
            if( user == null ) {
                txn.rollback();
                LOG.warning(USER_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_DOES_NOT_EXIST).build();
            }

            Key feedKey;
            Entity eventAux;
            String id;
            do {
                id = UUID.randomUUID().toString();
                feedKey = datastore.newKeyFactory().addAncestor(PathElement.of(USER, decodedToken.getUid())).setKind("PersonalEvent").newKey(id);
                eventAux = txn.get(feedKey);
            } while (eventAux != null);

            Key eventKey = datastore.newKeyFactory().addAncestor(PathElement.of(USER, decodedToken.getUid())).setKind("PersonalEvent").newKey(id);
            Entity event = txn.get(eventKey);

            event = Entity.newBuilder(eventKey)
                    .set("id", id)
                    .set("title", data.title)
                    .set("username", data.username)
                    .set("beginningDate", data.beginningDate)
                    .set("hours", data.hours)
                    .set("location", data.location)
                    .set("time_created", Timestamp.now())
                    .set("time_lastupdate", Timestamp.now())
                    .build();

            txn.add(event);
            LOG.info("Personal event added.");
            txn.commit();
            return Response.ok(id).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @GET
    @Path("/personalEvent/get/{id}")
    @Consumes(MediaType.APPLICATION_JSON)                                        //list composta por string que tem valor: "#papel-username"
    public Response getPersonalEvent(@HeaderParam("Authorization") String token, @PathParam("id") String id){
        LOG.fine("Attempt to get a personal event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        Transaction txn = datastore.newTransaction();

        Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
        Entity user = txn.get(userKey);
        if( user == null ) {
            txn.rollback();
            LOG.warning(USER_DOES_NOT_EXIST);
            return Response.status(Response.Status.BAD_REQUEST).entity(USER_DOES_NOT_EXIST).build();
        }
        Key eventKey = datastore.newKeyFactory().addAncestor(PathElement.of(USER, decodedToken.getUid())).setKind("PersonalEvent").newKey(id);
        Entity event = txn.get(eventKey);
        if(event == null) {
            txn.rollback();
            LOG.warning("User does not have this event.");
            return Response.status(Response.Status.BAD_REQUEST).entity("User does not have this event.").build();
        }

        PersonalEventsData data = new PersonalEventsData();
        // Enquanto não virmos quais os atributos a devolver em cada caso, vamos dar poucos
        data.id = event.getString("id");
        data.title = event.getString("title");
        data.username = event.getString("username");
        data.beginningDate = event.getString("beginningDate");
        data.hours = event.getString("hours");
        data.location = event.getString("location");

        LOG.fine("Personal event successfully obtained.");
        return Response.ok(g.toJson(data)).build();
    }


    @GET
    @Path("/personalEvent/monthly/{monthAndYear}")
    @Consumes(MediaType.APPLICATION_JSON)                                        //list composta por string que tem valor: "#papel-username"
    public Response getPersonalEventByMonth(@HeaderParam("Authorization") String token, @PathParam("monthAndYear") String monthAndYear){
        LOG.fine("Attempt to get a personal event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        Transaction txn = datastore.newTransaction();

        Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
        Entity user = txn.get(userKey);
        if( user == null ) {
            txn.rollback();
            LOG.warning(USER_DOES_NOT_EXIST);
            return Response.status(Response.Status.BAD_REQUEST).entity(USER_DOES_NOT_EXIST).build();
        }


        // Update department's children
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("PersonalEvent")
                .setFilter(StructuredQuery.PropertyFilter.hasAncestor(userKey))
                .build();

        QueryResults<Entity> queryResults = datastore.run(query);
        Entity memberEntity;
        List<PersonalEventsData> result = new ArrayList<>();
        while (queryResults.hasNext()) {
            memberEntity = queryResults.next();
            if(memberEntity.getString("beginningDate").contains("-" + monthAndYear)){
                PersonalEventsData data = new PersonalEventsData();
                data.id = memberEntity.getString("id");
                data.title = memberEntity.getString("title");
                data.username = memberEntity.getString("username");
                data.beginningDate = memberEntity.getString("beginningDate");
                data.hours = memberEntity.getString("hours");
                data.location = memberEntity.getString("location");
                result.add(data);
            }
        }
        // Enquanto não virmos quais os atributos a devolver em cada caso, vamos dar poucos


        LOG.fine("Personal events successfully gotten");
        return Response.ok(g.toJson(result)).build();
    }

    @POST
    @Path("/personalEvent/edit/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editPersonalEvent(@HeaderParam("Authorization") String token, @PathParam("id") String id, PersonalEventsData data){
        LOG.fine("Attempt to edit a personal event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
            Entity user = txn.get(userKey);
            if( user == null ) {
                txn.rollback();
                LOG.warning(USER_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_DOES_NOT_EXIST).build();
            }
            Key eventKey = datastore.newKeyFactory().addAncestor(PathElement.of(USER, decodedToken.getUid())).setKind("PersonalEvent").newKey(id);
            Entity event = txn.get(eventKey);
            if(event == null) {
                txn.rollback();
                LOG.warning("User does not have this event.");
                return Response.status(Response.Status.BAD_REQUEST).entity("User does not have this event.").build();
            }
            Entity updatedEvent = Entity.newBuilder(event)
                    .set("title", data.title)
                    .set("beginningDate", data.beginningDate)
                    .set("hours", data.hours)
                    .set("location", data.location)
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.update(updatedEvent);
            LOG.info("Personal event edited.");
            txn.commit();
            return Response.ok(id).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/personalEvent/delete/{id}")
    @Consumes(MediaType.APPLICATION_JSON)                                        //list composta por string que tem valor: "#papel-username"
    public Response deletePersonalEvent(@HeaderParam("Authorization") String token, @PathParam("id") String id){
        LOG.fine("Attempt to delete a personal event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
            Entity user = txn.get(userKey);
            if( user == null ) {
                txn.rollback();
                LOG.warning(USER_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_DOES_NOT_EXIST).build();
            }
            Key eventKey = datastore.newKeyFactory().addAncestor(PathElement.of(USER, decodedToken.getUid())).setKind("PersonalEvent").newKey(id);
            Entity event = txn.get(eventKey);
            if(event == null) {
                txn.rollback();
                LOG.warning("User does not have this event.");
                return Response.status(Response.Status.BAD_REQUEST).entity("User does not have this event.").build();
            }
            txn.delete(eventKey);
            LOG.info("Personal event deleted.");
            txn.commit();
            return Response.ok(id).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryUsers(@HeaderParam("Authorization") String token,
                               @QueryParam("limit") String limit,
                               @QueryParam("offset") String cursor, Map<String, String> filters){
        LOG.fine("Attempt to query users.");

        //Verificar, caso for evento privado, se o token é valido
        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        String role = getRole(decodedToken);
        if(!role.equals(BO) && !role.equals(ADMIN)){  //SE CALHAR PODE SE POR ROLE MINIMO COMO PROFESSOR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
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

        EntityQuery.Builder query = Query.newEntityQueryBuilder()
                .setKind(USER)
                .setFilter(attributeFilter)
                .setLimit(Integer.parseInt(limit));

        if ( !cursor.equals("EMPTY") ){
            query.setStartCursor(Cursor.fromUrlSafe(cursor));
        }

        queryResults = datastore.run(query.build());

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        LOG.info("Query de users pedido");
        Gson g = new Gson();
        return Response.ok(g.toJson(results))
                .header("X-Cursor",queryResults.getCursorAfter().toUrlSafe())
                .build();

    }

    @POST
    @Path("/numberOfUsers")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryUsersNum(@HeaderParam("Authorization") String token, Map<String, String> filters) {
        LOG.fine("Attempt to count the query users");

        // Verificar, caso for evento privado, se o token é valido

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
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
                .setKind(USER)
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
        // Convert the response object to JSON
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(count);

        return Response.ok(jsonResponse).build();
    }

}