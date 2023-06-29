package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import util.PersonalEventsData;
import util.ProfileData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static util.FirebaseAuth.*;
import static util.Constants.*;

@Path("/profile")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ProfileResource {
    private static final Logger LOG = Logger.getLogger(ProfileResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public Gson g = new Gson();

    public ProfileResource() {}

    @GET
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getProfile(@HeaderParam("Authorization") String token,
                               @PathParam("username") String username){
        LOG.fine("Attempt to get profile by " + username);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
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

        if (!username.equals(requester)){
            userKey = datastore.newKeyFactory().setKind(USER).newKey(username);
            user = datastore.get(userKey);

            if( user == null ) {
                LOG.warning(USER_OR_PASSWORD_INCORRECT);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_OR_PASSWORD_INCORRECT).build();
            }
        }
        ProfileData data = new ProfileData();
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
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addPersonalEvent(@HeaderParam("Authorization") String token,
                                     PersonalEventsData data) {
        LOG.fine("Attempt to add a personal event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
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

            String list = user.getString(PERSONAL_EVENT_LIST);
            if(list.contains(data.title)) {
                txn.rollback();
                LOG.warning("Personal event name already used.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Personal event name already used.").build();
            }

            list = list.concat("#" + data.title + "%" + decodedToken.getUid() + "%" + data.beginningDate + "%" + data.hours + "%" + data.location);

            Entity updatedUser = Entity.newBuilder(user)
                    .set("personal_event_list", list)
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.update(updatedUser);
            txn.commit();

            LOG.info("Personal event added.");
            return Response.ok(updatedUser).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @GET
    @Path("/personalEvent/get/{title}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getPersonalEvent(@HeaderParam("Authorization") String token,
                                     @PathParam("title") String title){
        LOG.fine("Attempt to edit a personal event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Transaction txn = datastore.newTransaction();
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
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
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editPersonalEvent(@HeaderParam("Authorization") String token,
                                      @PathParam("oldTitle") String oldTitle,
                                      PersonalEventsData data){
        LOG.fine("Attempt to edit a personal event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
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
            list = list.replace(oldEvent, data.title + "%" + decodedToken.getUid() + "%" + data.beginningDate + "%" + data.hours + "%" + data.location);

            Entity updatedUser = Entity.newBuilder(user)
                    .set("personal_event_list", list)
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.update(updatedUser);
            txn.commit();

            LOG.info("Personal event edited.");
            return Response.ok(updatedUser).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @PATCH
    @Path("/personalEvent/delete/{oldTitle}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deletePersonalEvent(@HeaderParam("Authorization") String token,
                                        @PathParam("oldTitle") String oldTitle){
        LOG.fine("Attempt to delete a personal event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
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
            txn.commit();

            LOG.info("Personal event deleted.");
            return Response.ok(updatedUser).build();
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
                                 @QueryParam("offset") String offset, Map<String, String> filters){
        LOG.fine("Attempt to query users.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if(!decodedToken.getUid().equals(BO)) {
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
        }

        if( filters == null){
            filters = new HashMap<>(1);
        }

        StructuredQuery.CompositeFilter attributeFilter = null;
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
        QueryResults<Entity> queryResults = datastore.run(query);

        List<Entity> results = new ArrayList<>();
        queryResults.forEachRemaining(results::add);

        LOG.info("Ides receber um query ó filho!");
        Gson g = new Gson();
        return Response.ok(g.toJson(results)).build();

    }

    @POST
    @Path("/numberOfUsers")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryUsersNum(@HeaderParam("Authorization") String token,
                                  Map<String, String> filters) {
        LOG.fine("Attempt to count the query users");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if (filters == null) {
            filters = new HashMap<>(1);
        }

        StructuredQuery.CompositeFilter attributeFilter = null;
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

        QueryResults<Entity> queryResults = datastore.run(query);

        LOG.info("Received a query!");

        int count = 0;
        while (queryResults.hasNext()) {
            queryResults.next();
            count++;
        }

        return Response.ok(count).build();
    }

}

