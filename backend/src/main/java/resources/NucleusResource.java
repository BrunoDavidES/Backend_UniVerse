package resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import models.NucleusData;
import utils.QueryResponse;

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


/**
 * Resource class for handling nucleus-related operations.
 */
@Path("/nucleus")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class NucleusResource {
    private static final Logger LOG = Logger.getLogger(NucleusResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    public NucleusResource() { }


    /**
     * Registers a nucleus.
     *
     * @param token The authorization token.
     * @param data  The nucleus data.
     * @return The response indicating the success or failure of the registration.
     * It will return 401 error if there is no token.
     * It will return 400 error if there are any missing or wring parameters,
     * if the president given doesn't exist
     * or if the nucleus already exists.
     * It will return 403 error if the user role is different from backoffice or admin.
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@HeaderParam("Authorization") String token, NucleusData data) throws FirebaseAuthException {
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
        return registerNucleusValidation(decodedToken, data);
    }


    /**
     * Modifies an existing nucleus.
     *
     * @param token The authorization token.
     * @param data  The nucleus data.
     * @return The response indicating the success or failure of the modification.
     * It will return 401 error if there is no token.
     * It will return 400 error if there are any missing or wring parameters,
     * if the president doesn't exist
     * or the nucleus doesn't exist.
     * It will return 403 error if the user is not backoffice, admin or the nucleus president.
     */
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
        return modifyNucleusValidation(decodedToken, data);
    }


    /**
     * Deletes a nucleus.
     *
     * @param token The authorization token.
     * @param id    The ID of the nucleus to delete.
     * @return The response indicating the success or failure of the deletion.
     * It will return 401 error if there is no token.
     * It will return 400 error if the user is not backoffice or admin.
     */
    @DELETE
    @Path("/delete")
    public Response deleteNucleus(@HeaderParam("Authorization") String token, @QueryParam("id") String id){
        LOG.fine("Attempt to delete nucleus.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        return deleteNucleusValidation(decodedToken, id);
    }


    /**
     * Queries nuclei based on specified filters.
     *
     * @param token   The authorization token.
     * @param limit   The maximum number of results to return.
     * @param cursor  The cursor for pagination.
     * @param filters The filters to apply in the query.
     * @return The response containing the query results.
     */
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

        QueryResponse response = new QueryResponse();
        response.setResults(results);
        response.setCursor(queryResults.getCursorAfter().toUrlSafe());

        LOG.info("Query de núcleos pedido");
        Gson g = new Gson();

        return Response.ok(g.toJson(response)).build();
    }

    /**
     * Validates and performs nucleus registration.
     *
     * @param decodedToken The decoded authorization token.
     * @param data         The nucleus data.
     * @return The response indicating the success or failure of the registration.
     */
    private Response registerNucleusValidation(FirebaseToken decodedToken,  NucleusData data) throws FirebaseAuthException {
        Transaction txn = datastore.newTransaction();
        try {
            Key presidentKey = datastore.newKeyFactory().setKind(USER).newKey(data.getPresident());
            Entity president = txn.get(presidentKey);
            String presRole = getRole(firebaseAuth.getUser(data.getPresident()));

            if (president == null){
                txn.rollback();
                LOG.warning(WRONG_PRESIDENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PRESIDENT).build();
            }
            if(!presRole.equals(STUDENT)){
                txn.rollback();
                LOG.warning(NOT_A_STUDENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(NOT_A_STUDENT).build();
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
                return registerNucleus(txn, data, president, nucleusKey);
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Performs nucleus registration.
     *
     * @param txn         The transaction to use.
     * @param data        The nucleus data.
     * @param president   The president entity.
     * @param nucleusKey  The key of the nucleus entity.
     * @return The response indicating the success or failure of the registration.
     */
    private Response registerNucleus(Transaction txn, NucleusData data, Entity president, Key nucleusKey){
        president = Entity.newBuilder(president)
                .set("nucleus", data.getId())
                .set("nucleus_job", "President")
                .build();
        txn.update(president);

        Entity nucleus = Entity.newBuilder(nucleusKey)
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

    /**
     * Validates and performs nucleus modification.
     *
     * @param decodedToken The decoded authorization token.
     * @param data         The nucleus data.
     * @return The response indicating the success or failure of the modification.
     */
    private Response modifyNucleusValidation(FirebaseToken decodedToken, NucleusData data){
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

            return modifyNucleus(data, txn, nucleus, modifierUsername, prevPresident);
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Performs nucleus modification.
     *
     * @param data              The nucleus data.
     * @param txn               The transaction to use.
     * @param nucleus           The nucleus entity.
     * @param modifierUsername  The username of the modifier.
     * @param prevPresident     The previous president username.
     * @return The response indicating the success or failure of the modification.
     */
    private Response modifyNucleus(NucleusData data, Transaction txn, Entity nucleus, String modifierUsername, String prevPresident){
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
    }

    /**
     * Validates and performs nucleus deletion.
     *
     * @param decodedToken The decoded authorization token.
     * @param id           The ID of the nucleus to delete.
     * @return The response indicating the success or failure of the deletion.
     */
    private Response deleteNucleusValidation(FirebaseToken decodedToken, String id){
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
                return deleteNucleus(txn, nucleusKey, id);
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Performs nucleus deletion.
     *
     * @param txn         The transaction to use.
     * @param nucleusKey  The key of the nucleus entity.
     * @param id          The ID of the nucleus to delete.
     * @return The response indicating the success or failure of the deletion.
     */
    private Response deleteNucleus(Transaction txn, Key nucleusKey, String id){
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
}