package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import models.PersonalEventsData;
import models.ProfileData;
import utils.QueryResponse;

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

    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    // Talvez adicionar LinkedIn

    @GET
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getProfile(@HeaderParam("Authorization") String token, @PathParam("username") String username) throws FirebaseAuthException {
        LOG.fine("Attempt to get profile by " + username);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
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

        data.setUsername(username);
        data.setName(user.getString("name"));
        data.setEmail(user.getString("email"));
        data.setRole(getRole(firebaseAuth.getUser(username)));
        data.setDepartment(user.getString("department"));
        data.setDepartment_job(user.getString("department_job"));
        data.setPhone((user.getString("phone")));
        data.setLinkedIn(user.getString("linkedin"));
        data.setPrivacy(user.getString("privacy"));
        data.setTimeCreation(user.getString("time_creation"));

        if (data.getRole().equals(STUDENT)){
            data.setNucleus(user.getString("nucleus"));
            data.setNucleus_job(user.getString("nucleus_job"));
        } else {
            data.setNucleus("");
            data.setNucleus_job("");
        }

        if (data.getRole().equals(TEACHER)){
            data.setOffice(user.getString("office"));
        } else
            data.setOffice("");

        String requesterUsername = decodedToken.getUid();
        String requesterRole = getRole(decodedToken);

        if ( requesterUsername.equals(username) || requesterRole.equals(BO) || requesterRole.equals(ADMIN) ){
            data.setLicense_plate(user.getString("license_plate"));
            data.setStatus(user.getString("status"));
        } else {
            data.setLicense_plate("");;
            data.setStatus("");
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
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

            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.getDepartment());
            Entity department = txn.get(departmentKey);
            if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            }
            Key eventKey = datastore.newKeyFactory().addAncestor(PathElement.of(USER, decodedToken.getUid())).setKind("PersonalEvent").newKey(id);
            Entity event;

            event = Entity.newBuilder(eventKey)
                    .set("id", id)
                    .set("title", data.getTitle())
                    .set("username", data.getUsername())
                    .set("beginningDate", data.getBeginningDate())
                    .set("hours", data.getHours())
                    .set("location", data.getLocation())
                    .set("department", data.getDepartment())
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
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
        data.setId(event.getString("id"));
        data.setTitle(event.getString("title"));
        data.setUsername(event.getString("username"));
        data.setDepartment(event.getString("department"));
        data.setBeginningDate(event.getString("beginningDate"));
        data.setHours(event.getString("hours"));
        data.setLocation(event.getString("location"));

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
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
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
                data.setId(memberEntity.getString("id"));
                data.setTitle(memberEntity.getString("title"));
                data.setUsername(memberEntity.getString("username"));
                data.setBeginningDate(memberEntity.getString("beginningDate"));
                data.setHours(memberEntity.getString("hours"));
                data.setLocation(memberEntity.getString("location"));
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
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
            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.getDepartment());
            Entity department = txn.get(departmentKey);
            if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            }
            Entity updatedEvent = Entity.newBuilder(event)
                    .set("title", data.getTitle())
                    .set("beginningDate", data.getBeginningDate())
                    .set("hours", data.getHours())
                    .set("location", data.getLocation())
                    .set("department", data.getDepartment())
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
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
            LOG.warning(TOKEN_NOT_FOUND + " token: " + token);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
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

        QueryResponse response = new QueryResponse();
        response.setResults(results);
        response.setCursor(queryResults.getCursorAfter().toUrlSafe());

        LOG.info("Query de users pedido");
        Gson g = new Gson();
        return Response.ok(g.toJson(response)).build();

    }

    @POST
    @Path("/query/public")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryPublicUsers(@HeaderParam("Authorization") String token,
                                     @QueryParam("limit") String limit,
                                     @QueryParam("offset") String cursor) {
        LOG.fine("Attempt to query public users.");

        FirebaseToken decodedToken = authenticateToken(token);
        if (decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND + " token: " + token);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        QueryResults<Entity> queryResults;

        EntityQuery.Builder query = Query.newEntityQueryBuilder()
                .setKind(USER)
                .setFilter(StructuredQuery.PropertyFilter.eq("privacy", "PUBLIC"))
                .setLimit(Integer.parseInt(limit));

        if (!cursor.equals("EMPTY")) {
            query.setStartCursor(Cursor.fromUrlSafe(cursor));
        }

        queryResults = datastore.run(query.build());

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        QueryResponse response = new QueryResponse();
        response.setResults(results);
        response.setCursor(queryResults.getCursorAfter().toUrlSafe());

        LOG.info("Query de public users pedido");
        Gson g = new Gson();
        return Response.ok(g.toJson(response)).build();
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
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