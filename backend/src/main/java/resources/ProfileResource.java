package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.*;
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


/**
 * The ProfileResource class represents a resource class for handling profile-related operations.
 */
@Path("/profile")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ProfileResource {
    private static final Logger LOG = Logger.getLogger(ProfileResource.class.getName());

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public Gson g = new Gson();

    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    /**
     * Retrieves the profile of a user.
     *
     * @param token    The authorization token.
     * @param username The username of the user whose profile is requested.
     * @return The response containing the profile data.
     * @throws FirebaseAuthException if there is an error in Firebase authentication.
     * It will return 401 if the token doesn't exist.
     * It will return 400 if the username param is empty or null,
     * if the requester doesn't exist
     * or if the user of the profile getting requested doesn't exist.
     */

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
        return getUserValidation(decodedToken, username);
    }

    /**
     * Adds a personal event for a user.
     *
     * @param token The authorization token.
     * @param data  The data of the personal event to be added.
     * @return The response indicating the success or failure of the operation.
     * It will return 401 if the token doesn't exist.
     * It will return 400 if the user doesn't exist
     * or if the personal event department doesn't exist.
     */
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
        return addPEventValidation(decodedToken, data);
    }

    /**
     * Retrieves a personal event by its ID.
     *
     * @param token The authorization token.
     * @param id    The ID of the personal event.
     * @return The response containing the personal event data.
     * It will return 401 if the token doesn't exist.
     * It will return 400 error if the user doesn't exist
     * or if the user doesn't have this personal event.
     */
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
        return getPEventValidation(decodedToken, id);
    }


    /**
     * Retrieves personal events for a specific month and year.
     *
     * @param token         The authorization token.
     * @param monthAndYear  The month and year (in the format MM-yyyy) for which to retrieve personal events.
     * @return The response containing the personal events for the specified month and year.
     * It will return 401 if the token doesn't exist.
     * It will return 400 error if the user doesn't exist.
     */
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
        return getMonthlyPEvents(userKey, monthAndYear);
    }

    /**
     * Edits a personal event.
     *
     * @param token The authorization token.
     * @param id    The ID of the personal event to be edited.
     * @param data  The updated data for the personal event.
     * @return The response indicating the success or failure of the operation.
     * It will return 401 if the token doesn't exist.
     * It will return 400 error if the user doesn't exist,
     * if the user doesn't have this personal event
     * or if the new department of this personal event doesn't exist.
     */
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
        return editPEventValidations(decodedToken, data, id);
    }

    /**
     * Deletes a personal event.
     *
     * @param token The authorization token.
     * @param id    The ID of the personal event to be deleted.
     * @return The response indicating the success or failure of the operation.
     * It will return 401 if the token doesn't exist.
     * It will return 400 if the user doesn't exist
     * or if the user doesn't have this personal event.
     */
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
        return deletePEvent(decodedToken, id);
    }

    /**
     * Performs a query to retrieve users based on specified filters.
     *
     * @param token    The authorization token.
     * @param limit    The maximum number of results to return.
     * @param cursor   The cursor to continue the query from (optional).
     * @param filters  The filters to apply to the query.
     * @return The response containing the query results.
     * It will return 401 if the token doesn't exist.
     * It will return 400 error if the user role is different from backoffice or admin.
     */
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
        return prepareFilters(decodedToken, filters, limit, cursor);

    }

    /**
     * Performs a query to retrieve public users.
     *
     * @param token  The authorization token.
     * @param limit  The maximum number of results to return.
     * @param cursor The cursor to continue the query from (optional).
     * @return The response containing the query results for public users.
     * It will return 401 if the token doesn't exist.
     *
     */
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
        return queryPublic(limit, cursor);
    }

    /**
     * Performs a query to retrieve the number of users based on specified filters.
     *
     * @param token   The authorization token.
     * @param filters The filters to apply to the query.
     * @return The response containing the number of users matching the filters.
     * It will return 401 if the token doesn't exist.
     */
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
        return prepareUsersNumFilters(filters);
    }

    /**
     * Retrieves the count of logged-in users within the last 30 minutes.
     *
     * @param token The authorization token.
     * @return The response containing the count of logged-in users.
     * It will return 401 if the token doesn't exist.
     * It will return 400 error if the user role is different from backoffice or admin.
     */
    @GET
    @Path("/loggedinuserscount")
    public Response getLoggedInUsersCount(@HeaderParam("Authorization") String token) {
        LOG.fine("Attempt to count the number of logged in users");

        // Verificar, caso for evento privado, se o token é valido

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        String role = getRole(decodedToken);
        if(!role.equals(BO) && !role.equals(ADMIN)){  //SE CALHAR PODE SE POR ROLE MINIMO COMO PROFESSOR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
        }
        return loggsLast30Mins();
    }

    /**
     * Endpoint for verifying a backoffice token.
     *
     * @param token The authorization token.
     * @return A response indicating whether the token is a valid BO token.
     */
    @GET
    @Path("/verifyBOToken")
    public Response verifyBOToken(@HeaderParam("Authorization") String token){
        FirebaseToken decodedToken = authenticateToken(token);
        if (decodedToken == null){
            return Response.ok("false").build();
        }
        else if (!getRole(decodedToken).equals(BO) && !getRole(decodedToken).equals(ADMIN)){
            return Response.ok("false").build();
        }
        return Response.ok("true").build();
    }

    /**
     * Validates the user and retrieves the profile data.
     *
     * @param decodedToken The decoded Firebase token.
     * @param username     The username of the user.
     * @return The response containing the user's profile data.
     * @throws FirebaseAuthException if there is an error in Firebase authentication.
     */
    private Response getUserValidation(FirebaseToken decodedToken, String username) throws FirebaseAuthException {
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
        return getUser(decodedToken, data, user, username);
    }

    /**
     * Retrieves the user's profile data.
     *
     * @param decodedToken The decoded Firebase token.
     * @param data         The profile data to be filled.
     * @param user         The user's entity.
     * @param username     The username of the user.
     * @return The response containing the user's profile data.
     * @throws FirebaseAuthException if there is an error in Firebase authentication.
     */
private Response getUser(FirebaseToken decodedToken, ProfileData data, Entity user, String username) throws FirebaseAuthException {
    data.setUsername(username);
    data.setName(user.getString("name"));
    data.setEmail(user.getString("email"));
    data.setRole(getRole(firebaseAuth.getUser(username)));
    data.setDepartment(user.getString("department"));
    data.setDepartment_job(user.getString("department_job"));
    data.setPhone((user.getString("phone")));
    data.setLinkedIn(user.getString("linkedin"));
    data.setPrivacy(user.getString("privacy"));
    data.setTimeCreation(user.getTimestamp("time_creation").toString());

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

    /**
     * Validates the data and adds a personal event for the user.
     *
     * @param decodedToken The decoded Firebase token.
     * @param data         The data of the personal event to be added.
     * @return The response indicating the result of the operation.
     */
private Response addPEventValidation(FirebaseToken decodedToken, PersonalEventsData data){
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
        return addPEvent(decodedToken, data, txn, id);
    } finally {
        if (txn.isActive()) {
            txn.rollback();
        }
    }
}

    /**
     * Adds a personal event for the user.
     *
     * @param decodedToken The decoded Firebase token.
     * @param data         The data of the personal event to be added.
     * @param txn          The transaction for the datastore operation.
     * @param id           The ID of the personal event.
     * @return The response indicating the result of the operation.
     */
private Response addPEvent(FirebaseToken decodedToken, PersonalEventsData data, Transaction txn, String id){
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
}


    /**
     * Validates the data and retrieves a personal event for the user.
     *
     * @param decodedToken The decoded Firebase token.
     * @param id           The ID of the personal event.
     * @return The response containing the personal event.
     */
private Response getPEventValidation(FirebaseToken decodedToken, String id){
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
        LOG.warning(WRONG_PERSONAL_EVENT);
        return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PERSONAL_EVENT).build();
    }
    return getPEvent(event);
}

    /**
     * Retrieves a personal event.
     *
     * @param event The entity representing the personal event.
     * @return The response containing the personal event.
     */
private Response getPEvent(Entity event){
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


    /**
     * Retrieves the personal events of a user for a specific month and year.
     *
     * @param userKey         The key of the entity user.
     * @param monthAndYear  The month and year (formatted as "MM-yyyy") for filtering the events.
     * @return The response containing the personal events for the specified month and year.
     */
private Response getMonthlyPEvents(Key userKey, String monthAndYear){
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

    /**
     * Validates the data and updates a personal event.
     *
     * @param decodedToken The decoded Firebase token.
     * @param id           The ID of the personal event.
     * @param data         The updated data of the personal event.
     * @return The response indicating the result of the operation.
     */
private Response editPEventValidations(FirebaseToken decodedToken, PersonalEventsData data, String id){
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
            LOG.warning(WRONG_PERSONAL_EVENT);
            return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PERSONAL_EVENT).build();
        }
        Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.getDepartment());
        Entity department = txn.get(departmentKey);
        if( department == null ) {
            txn.rollback();
            LOG.warning(WRONG_DEPARTMENT);
            return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
        }
        return editPEvent(txn, data, event, id);
    } finally {
        if (txn.isActive()) {
            txn.rollback();
        }
    }
}


    /**
     * Updates a personal event in the datastore.
     *
     * @param txn   The transaction object.
     * @param data  The updated data of the personal event.
     * @param event The entity of the personal event to update.
     * @param id    The ID of the personal event.
     * @return The response indicating the result of the operation.
     */
private Response editPEvent(Transaction txn, PersonalEventsData data, Entity event, String id){
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
}

    /**
     * Deletes a personal event.
     *
     * @param decodedToken The decoded Firebase token.
     * @param id           The ID of the personal event.
     * @return The response indicating the result of the operation.
     */
private Response deletePEvent(FirebaseToken decodedToken, String id){
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
            LOG.warning(WRONG_PERSONAL_EVENT);
            return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PERSONAL_EVENT).build();
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

    /**
     * Prepares the filters for querying users.
     *
     * @param decodedToken The decoded Firebase token.
     * @param filters      The filters to apply to the query.
     * @param limit        The maximum number of results to return.
     * @param cursor       The cursor for paginating through the results.
     * @return The response containing the query results.
     */
private Response prepareFilters(FirebaseToken decodedToken, Map<String, String> filters, String limit, String cursor){
    String role = getRole(decodedToken);
    if(!role.equals(BO) && !role.equals(ADMIN)){  //SE CALHAR PODE SE POR ROLE MINIMO COMO PROFESSOR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        LOG.warning(NICE_TRY);
        return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
    }
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

    return queryUsers(attributeFilter, limit, cursor);
}

    /**
     * Queries users based on the provided filters, limit, and cursor.
     *
     * @param attributeFilter The composite filter to apply to the query.
     * @param limit           The maximum number of results to return.
     * @param cursor          The cursor for paginating through the results.
     * @return The response containing the query results.
     */
private Response queryUsers(StructuredQuery.CompositeFilter attributeFilter, String limit, String cursor){

    QueryResults<Entity> queryResults;
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

    /**
     * Queries public users based on the provided limit and cursor.
     *
     * @param limit  The maximum number of results to return.
     * @param cursor The cursor for paginating through the results.
     * @return The response containing the query results.
     */
private Response queryPublic(String limit, String cursor){
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


    /**
     * Prepares the filters for querying the number of users.
     *
     * @param filters The filters to apply to the query.
     * @return The response containing the number of users matching the filters.
     */
private Response prepareUsersNumFilters(Map<String, String> filters){

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
    return usersNum(attributeFilter);
}

    /**
     * Queries the number of users based on the provided filters.
     *
     * @param attributeFilter The composite filter to apply to the query.
     * @return The response containing the number of users matching the filters.
     */
private Response usersNum(StructuredQuery.CompositeFilter attributeFilter){

    QueryResults<Key> queryResults;
    //Query<Entity> query = Query.newEntityQueryBuilder()
    Query<Key> query = Query.newKeyQueryBuilder()
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


    /**
     * Retrieves the count of logged-in users in the last 30 minutes.
     *
     * @return The response containing the count of logged-in users.
     */
private Response loggsLast30Mins(){
    try {
        long currentTimeMillis = System.currentTimeMillis();
        int loggedInUsersCount = 0;
        ListUsersPage page = firebaseAuth.listUsers(null);
        for (UserRecord userRecord : page.iterateAll()) {
            long lastSignInTimeMillis = userRecord.getUserMetadata().getLastSignInTimestamp();
            if (lastSignInTimeMillis > 0) {
                // Set the time threshold as needed (e.g., consider users logged in within the last 30 minutes)
                long timeThresholdMillis = 30 * 60 * 1000; // 5 minutes

                if (currentTimeMillis - lastSignInTimeMillis <= timeThresholdMillis) {
                    loggedInUsersCount++;
                }
            }
        }

        return Response.ok(loggedInUsersCount).build();
    } catch (FirebaseAuthException e) {
        LOG.severe("Error retrieving logged-in users count: " + e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
}