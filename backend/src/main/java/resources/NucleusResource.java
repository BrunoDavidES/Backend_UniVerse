package resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import models.NucleusData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.FirebaseAuth.authenticateToken;
import static utils.FirebaseAuth.getRole;

@Path("/nucleus")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class NucleusResource {
    private static final Logger LOG = Logger.getLogger(NucleusResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public NucleusResource() { }


    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@HeaderParam("Authorization") String token, NucleusData data) {
        LOG.fine("Attempt to create a nucleus by: " + data.getPresident());

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        if( !data.validateRegister() ) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key presidentKey = datastore.newKeyFactory().setKind(USER).newKey(data.getPresident());
            Entity president = txn.get(presidentKey);

            if (president == null){
                txn.rollback();
                LOG.warning(WRONG_PRESIDENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PRESIDENT).build();
            }

            String role = getRole(decodedToken);

            if (!role.equals(BO) && !role.equals(ADMIN)) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
            }

            Key nucleusKey = datastore.newKeyFactory().setKind(NUCLEUS).newKey(data.getId());
            Entity nucleus = txn.get(nucleusKey);

            if( nucleus != null ) {
                txn.rollback();
                LOG.warning(NUCLEUS_ALREADY_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_ALREADY_EXISTS).build();
            } else {
                president = Entity.newBuilder(president)
                        .set("nucleus", data.getId())
                        .set("nucleus_job", "President")
                        .build();
                txn.update(president);

                nucleus = Entity.newBuilder(nucleusKey)
                        .set("email", data.getNucleusEmail())
                        .set("name", data.getName())
                        .set("id", data.getId())
                        .set("location", data.getLocation())
                        .set("president", data.getPresident())
                        .set("website", "")
                        .set("instagram", "")
                        .set("twitter", "")
                        .set("facebook", "")
                        .set("youtube", "")
                        .set("linkedIn", "")
                        .set("description", "")
                        .set("time_creation", Timestamp.now())
                        .set("time_lastupdate", Timestamp.now())
                        .build();
                txn.add(nucleus);

                LOG.info("Nucleus registered: " + data.getId() + "| " + data.getName());
                txn.commit();
                return Response.ok(nucleus).entity("Nucleus registered").build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


    @POST
    @Path("/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyNucleus(@HeaderParam("Authorization") String token, NucleusData data){
        LOG.fine("Attempt to modify nucleus.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        if( !data.validateModify()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key nucleusKey = datastore.newKeyFactory().setKind(NUCLEUS).newKey(data.getId());
            Entity nucleus = txn.get(nucleusKey);

            if( nucleus == null ) {
                txn.rollback();
                LOG.warning(NUCLEUS_DOES_NOT_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_DOES_NOT_EXISTS).build();
            }

            String modifierUsername = decodedToken.getUid();
            String modifierRole = getRole(decodedToken);
            String prevPresident = nucleus.getString("president");

            if (!modifierRole.equals(BO) && !modifierRole.equals(ADMIN) && !prevPresident.equals(modifierUsername)) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
            }

            data.fillGaps(nucleus);

            if (!modifierUsername.equals(data.getPresident())){
                Key presidentKey = datastore.newKeyFactory().setKind(USER).newKey(data.getPresident());
                Entity president = txn.get(presidentKey);

                if (president == null){
                    txn.rollback();
                    LOG.warning(WRONG_PRESIDENT);
                    return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PRESIDENT).build();
                }

                Key previousPresidentKey = datastore.newKeyFactory().setKind(USER).newKey(prevPresident);

                Entity newPreviousPresident = Entity.newBuilder(txn.get(previousPresidentKey))
                        .set("nucleus_job", "Member")
                        .build();
                txn.update(newPreviousPresident);

                Entity newPresident = Entity.newBuilder(president)
                        .set("nucleus", data.getId())
                        .set("nucleus_job", "Presidente")
                        .build();
                txn.update(newPresident);
            }

            Entity newNucleus = Entity.newBuilder(nucleus)
                    .set("name", data.getNewName())
                    .set("id", data.getId())
                    .set("location", data.getLocation())
                    .set("president", data.getPresident())
                    .set("email", data.getNucleusEmail())
                    .set("website", data.getWebsite())
                    .set("instagram", data.getInstagram())
                    .set("twitter", data.getTwitter())
                    .set("facebook", data.getFacebook())
                    .set("youtube", data.getYoutube())
                    .set("linkedIn", data.getLinkedIn())
                    .set("description", data.getDescription())
                    .set("time_lastupdate", Timestamp.now())
                    .build();

            txn.update(newNucleus);

            LOG.info("Nucleus " + data.getName() + " has been edited.");
            txn.commit();
            return Response.ok(newNucleus).entity("Nucleus edited successfully").build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/delete")
    public Response deleteNucleus(@HeaderParam("Authorization") String token, @QueryParam("id") String id){
        LOG.fine("Attempt to delete nucleus.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key nucleusKey = datastore.newKeyFactory().setKind(NUCLEUS).newKey(id);
            Entity nucleus = txn.get(nucleusKey);

            String role = getRole(decodedToken);
            if(!role.equals(BO) && !role.equals(ADMIN)){
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }else if( nucleus == null ) {
                txn.rollback();
                LOG.warning(NUCLEUS_DOES_NOT_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_DOES_NOT_EXISTS).build();
            } else {
                Query<Entity> query = Query.newEntityQueryBuilder()
                        .setKind("User")
                        .setFilter(StructuredQuery.PropertyFilter.eq("nucleus", id))
                        .build();

                QueryResults<Entity> queryResults = datastore.run(query);
                while (queryResults.hasNext()) {
                    Entity userEntity = queryResults.next();
                    userEntity = Entity.newBuilder(userEntity)
                            .set("nucleus", "")
                            .set("nucleus_job", "")
                            .build();
                    txn.update(userEntity);
                }
                txn.delete(nucleusKey);
                LOG.info("Nucleus deleted.");
                txn.commit();
                return Response.ok(nucleusKey).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryNucleus(@HeaderParam("Authorization") String token,
                                 @QueryParam("limit") String limit,
                                 @QueryParam("offset") String cursor, Map<String, String> filters){
        LOG.fine("Attempt to query nucleus.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
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

        EntityQuery.Builder query = Query.newEntityQueryBuilder()
                .setKind(NUCLEUS)
                .setFilter(attributeFilter)
                .setLimit(Integer.parseInt(limit));

        if ( !cursor.equals("EMPTY") ){
            query.setStartCursor(Cursor.fromUrlSafe(cursor));
        }

        queryResults = datastore.run(query.build());

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        LOG.info("Query de núcleos pedido");
        Gson g = new Gson();

        return Response.ok(g.toJson(results))
                .header("X-Cursor",queryResults.getCursorAfter().toUrlSafe())
                .build();
    }
}