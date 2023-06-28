package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import util.DepartmentData;
import util.PersonalEventsData;
import util.ProfileData;
import util.ValToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/profile")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ProfileResource {

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


    private static final String BO = "BO";
    private static final String D = "D";
    private static final String A = "A";
    private static final String ROLE = "role";
    private static final String USER = "User";
    private static final String EVENT = "Event";
    private static final String NEWS = "News";
    private static final String STUDENTS_UNION = "Students Union";
    private static final String USER_CLAIM = "user";
    private static final String NAME_CLAIM = "name";
    private static final String PERSONAL_EVENT_LIST = "personal_event_list";
    private static final String MISSING_OR_WRONG_PARAMETER = "Missing or wrong parameter.";
    private static final String MISSING_PARAMETER = "Missing parameter.";
    private static final String TOKEN_NOT_FOUND = "Token not found.";
    private static final String USER_DOES_NOT_EXIST = "User does not exist.";
    private static final String ENTITY_DOES_NOT_EXIST = "Entity does not exist.";
    private static final String ONE_OF_THE_USERS_DOES_NOT_EXIST = "One of the users does not exist.";
    private static final String USER_OR_PASSWORD_INCORRECT = "User or password incorrect.";
    private static final String PASSWORD_INCORRECT = "Password incorrect.";
    private static final String NUCLEUS_DOES_NOT_EXISTS = "Nucleus does not exist.";
    private static final String NUCLEUS_ALREADY_EXISTS = "Nucleus already exists.";
    private static final String NICE_TRY = "Nice try but your not a capi person.";
    private static final String PERMISSION_DENIED = "Permission denied.";

    private static final String DEPARTMENT = "Department";
    private static final String WRONG_PRESIDENT = "President doesn't exists.";
    private static final String DEPARTMENT_ALREADY_EXISTS = "Department already exists.";
    private static final String WRONG_DEPARTMENT = "Department does not exist.";
    private static final String WRONG_MEMBER = "Member doesn't exists.";
    private static final Logger LOG = Logger.getLogger(ProfileResource.class.getName());

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public Gson g = new Gson();

    // Talvez adicionar LinkedIn

    @GET
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getProfile(@Context HttpServletRequest request, @PathParam("username") String username){
        LOG.fine("Attempt to get profile by " + username);

        if(username == null){
            LOG.warning("username field is null");
            return Response.status(Response.Status.BAD_REQUEST).entity("Empty param").build();
        }



        final ValToken validator = new ValToken();
        DecodedJWT token = validator.checkToken(request);

        if (token == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }
        String requester = String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", "");
        Key userKey = datastore.newKeyFactory().setKind(USER).newKey(requester);
        Entity user = datastore.get(userKey);

        if( user == null ) {
            LOG.warning(USER_DOES_NOT_EXIST);
            return Response.status(Response.Status.BAD_REQUEST).entity("User does not exist "  + requester).build();
        }

        // Vai ter de mudar quando se souber os atributos a devolver em cada caso
        if (!username.equals(requester)){
            // Faz um perfil menos completo
            userKey = datastore.newKeyFactory().setKind(USER).newKey(username);
            user = datastore.get(userKey);

            if( user == null ) {
                LOG.warning(USER_OR_PASSWORD_INCORRECT);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_OR_PASSWORD_INCORRECT).build();
            }
        }
        // Enquanto não virmos quais os atributos a devolver em cada caso, vamos dar poucos
        ProfileData data = new ProfileData();
        // Enquanto não virmos quais os atributos a devolver em cada caso, vamos dar poucos
        data.username = username;
        data.name = user.getString("name");
        data.email = user.getString("email");
        data.role = user.getString("role");
        data.jobs = user.getString("job_list");

        LOG.fine("Profile successfully gotten");
        return Response.ok(g.toJson(data)).build();
    }

    @POST
    @Path("/personalEvent/add")
    @Consumes(MediaType.APPLICATION_JSON)                                        //list composta por string que tem valor: "#papel-username"
    public Response addPersonalEvent(@Context HttpServletRequest request, PersonalEventsData data) {
        LOG.fine("Attempt to add a personal event.");

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""));
            Entity user = txn.get(userKey);
            if( user == null ) {
                txn.rollback();
                LOG.warning(USER_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_DOES_NOT_EXIST).build();
            }
            String list = user.getString(PERSONAL_EVENT_LIST);
            if(list.contains(data.title)) {
                txn.rollback();
                LOG.warning("Personal event name already used.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Personal event name already used.").build();
            }
            list = list.concat("#" + data.title + "%" + String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", "") + "%" + data.beginningDate + "%" + data.hours + "%" + data.location);

            Entity updatedUser = Entity.newBuilder(user)
                    .set("personal_event_list", list)
                    .set("time_lastupdate", Timestamp.now())
                    .build();

            txn.update(updatedUser);
            LOG.info("Personal event added.");
            txn.commit();
            return Response.ok(updatedUser).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @GET
    @Path("/personalEvent/get/{title}")
    @Consumes(MediaType.APPLICATION_JSON)                                        //list composta por string que tem valor: "#papel-username"
    public Response getPersonalEvent(@Context HttpServletRequest request,@PathParam("title") String title){
        LOG.fine("Attempt to edit a personal event.");

        Transaction txn = datastore.newTransaction();
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""));
            Entity user = txn.get(userKey);
            if( user == null ) {
                txn.rollback();
                LOG.warning(USER_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_DOES_NOT_EXIST).build();
            }
            String list = user.getString(PERSONAL_EVENT_LIST);
            if(!list.contains(title)) {
                txn.rollback();
                LOG.warning("Personal event does not exist.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Personal event does not exist.").build();
            }
            String[] l = list.split("#");
            String[] oldEvent = new String[5];
            for(String event: l){
                if(event.contains(title)){
                    oldEvent = event.replace("#", "").split("%");
                    break;
                }
            }
        PersonalEventsData data = new PersonalEventsData();
        // Enquanto não virmos quais os atributos a devolver em cada caso, vamos dar poucos
        data.title = oldEvent[0];
        data.username = oldEvent[1];
        data.beginningDate = oldEvent[2];
        data.hours = oldEvent[3];
        data.location = oldEvent[4];

        LOG.fine("Personal event successfully gotten");
        return Response.ok(g.toJson(data)).build();
    }

    @PATCH
    @Path("/personalEvent/edit/{oldTitle}")
    @Consumes(MediaType.APPLICATION_JSON)                                        //list composta por string que tem valor: "#papel-username"
    public Response editPersonalEvent(@Context HttpServletRequest request,@PathParam("oldTitle") String oldTitle, PersonalEventsData data){
        LOG.fine("Attempt to edit a personal event.");

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""));
            Entity user = txn.get(userKey);
            if( user == null ) {
                txn.rollback();
                LOG.warning(USER_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_DOES_NOT_EXIST).build();
            }
            String list = user.getString(PERSONAL_EVENT_LIST);
            if(!list.contains(oldTitle)) {
                txn.rollback();
                LOG.warning("Personal event does not exist.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Personal event does not exist.").build();
            }
            String[] l = list.split("#");
            String oldEvent = "";
            for(String event: l){
                if(event.contains(oldTitle)){
                    oldEvent = event;
                    break;
                }
            }
            list = list.replace(oldEvent, data.title + "%" + String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", "") + "%" + data.beginningDate + "%" + data.hours + "%" + data.location);

            Entity updatedUser = Entity.newBuilder(user)
                    .set("personal_event_list", list)
                    .set("time_lastupdate", Timestamp.now())
                    .build();

            txn.update(updatedUser);
            LOG.info("Personal event edited.");
            txn.commit();
            return Response.ok(updatedUser).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @PATCH
    @Path("/personalEvent/delete/{oldTitle}")
    @Consumes(MediaType.APPLICATION_JSON)                                        //list composta por string que tem valor: "#papel-username"
    public Response deletePersonalEvent(@Context HttpServletRequest request,@PathParam("oldTitle") String oldTitle){
        LOG.fine("Attempt to delete a personal event.");

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""));
            Entity user = txn.get(userKey);
            if( user == null ) {
                txn.rollback();
                LOG.warning(USER_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_DOES_NOT_EXIST).build();
            }
            String list = user.getString(PERSONAL_EVENT_LIST);
            if(!list.contains(oldTitle)) {
                txn.rollback();
                LOG.warning("Personal event does not exist.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Personal event does not exist.").build();
            }
            String[] l = list.split("#");
            String oldEvent = null;
            for(String event: l){
                if(event.contains(oldTitle)){
                    oldEvent = event;
                    break;
                }
            }
            list = list.replace("#" + oldEvent, "");

            Entity updatedUser = Entity.newBuilder(user)
                    .set("personal_event_list", list)
                    .set("time_lastupdate", Timestamp.now())
                    .build();

            txn.update(updatedUser);
            LOG.info("Personal event deleted.");
            txn.commit();
            return Response.ok(updatedUser).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
    //FAZER GETPERSONALEVENT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryUsers(@Context HttpServletRequest request,
                                 @QueryParam("limit") String limit,
                                 @QueryParam("offset") String offset, Map<String, String> filters){
        LOG.fine("Attempt to query users.");

        //Verificar, caso for evento privado, se o token é valido
        final ValToken validator = new ValToken();
        DecodedJWT token = validator.checkToken(request);

        if (token == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }
        if(!String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "").equals(BO)){  //SE CALHAR PODE SE POR ROLE MINIMO COMO PROFESSOR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
        }
        QueryResults<Entity> queryResults;

        StructuredQuery.CompositeFilter attributeFilter = null;
        if( filters == null){
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

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(USER)
                .setFilter(attributeFilter)
                .setLimit(Integer.parseInt(limit))
                .setOffset(Integer.parseInt(offset))
                .build();

        queryResults = datastore.run(query);

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        LOG.info("Ides receber um query ó filho!");
        Gson g = new Gson();
        return Response.ok(g.toJson(results)).build();

    }

    @POST
    @Path("/numberOfUsers")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryUsersNum(@Context HttpServletRequest request, Map<String, String> filters) {
        LOG.fine("Attempt to count the query users");

        // Verificar, caso for evento privado, se o token é valido

            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.info(TOKEN_NOT_FOUND);
                if (filters == null)
                    filters = new HashMap<>(1);
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

        List<Entity> results = new ArrayList<>();

        //queryResults.forEachRemaining(results::add);

        LOG.info("Received a query!");
        int count = 0;
        // Get the total number of entities
        while (queryResults.hasNext()) {
            results.add(queryResults.next());
            count++;
        }
        // Convert the response object to JSON
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(count);

        return Response.ok(jsonResponse).build();
    }

}

