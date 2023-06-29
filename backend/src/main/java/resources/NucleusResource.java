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

import static utils.FirebaseAuth.*;
import static utils.Constants.*;

@Path("/nucleus")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class NucleusResource {
    private static final Logger LOG = Logger.getLogger(NucleusResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public NucleusResource() { }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@HeaderParam("Authorization") String token,
                             NucleusData data) {
        LOG.fine("Attempt to create a nucleus by: " + data.president);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if( !data.validateRegister() ) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key presidentKey = datastore.newKeyFactory().setKind(USER).newKey(data.president);
            Entity president = txn.get(presidentKey);

            if (president == null){
                txn.rollback();
                LOG.warning(WRONG_PRESIDENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PRESIDENT).build();
            }

            String creatorUsername = decodedToken.getUid();
            String creatorRole = getRole(decodedToken);

            if (!creatorRole.equals(BO)) {
                if (creatorRole.equals(A)) {
                    Key studentsUnionKey = datastore.newKeyFactory().setKind(STUDENTS_UNION).newKey(STUDENTS_UNION);
                    Entity studentsUnion = txn.get(studentsUnionKey);

                    if (studentsUnion == null){
                        txn.rollback();
                        LOG.warning(ENTITY_DOES_NOT_EXIST);
                        return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
                    }
                    if (!studentsUnion.getString("president").equals(creatorUsername)){
                        txn.rollback();
                        LOG.warning(PERMISSION_DENIED);
                        return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
                    }
                }
                else {
                    txn.rollback();
                    LOG.warning(PERMISSION_DENIED);
                    return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
                }
            }

            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(data.id);
            Entity nucleus = txn.get(nucleusKey);

            if( nucleus != null ) {
                txn.rollback();
                LOG.warning(NUCLEUS_ALREADY_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_ALREADY_EXISTS).build();
            }

            nucleus = Entity.newBuilder(nucleusKey)
                    .set("email", data.nucleusEmail)
                    .set("name", data.name)
                    .set("id", data.id)
                    .set("president", data.president)
                    .set("website", "")
                    .set("instagram", "")
                    .set("twitter", "")
                    .set("facebook", "")
                    .set("youtube", "")
                    .set("linkedIn", "")
                    .set("description", "")
                    .set("members_list", "")
                    .set("time_creation", Timestamp.now())
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.add(nucleus);
            txn.commit();

            LOG.info("Nucleus registered: " + data.id + "| " + data.name);
            return Response.ok(nucleus).entity("Nucleus registered").build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyNucleus(@HeaderParam("Authorization") String token,
                                  NucleusData data){
        LOG.fine("Attempt to modify nucleus.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if( !data.validateModify()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(data.id);
            Entity nucleus = txn.get(nucleusKey);

            if( nucleus == null ) {
                txn.rollback();
                LOG.warning(NUCLEUS_DOES_NOT_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_DOES_NOT_EXISTS).build();
            }

            String modifierUsername = decodedToken.getUid();
            String modifierRole = getRole(decodedToken);

            if (!modifierRole.equals(BO)) {
                if (modifierRole.equals(A)) {
                    Key studentsUnionKey = datastore.newKeyFactory().setKind(STUDENTS_UNION).newKey(STUDENTS_UNION);
                    Entity studentsUnion = txn.get(studentsUnionKey);

                    if (studentsUnion == null){
                        txn.rollback();
                        LOG.warning(ENTITY_DOES_NOT_EXIST);
                        return Response.status(Response.Status.NOT_FOUND).entity(PERMISSION_DENIED).build();
                    }
                    if (!studentsUnion.getString("president").equals(modifierUsername) || !nucleus.getString("president").equals(modifierUsername) ){
                        txn.rollback();
                        LOG.warning(PERMISSION_DENIED);
                        return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
                    }
                }
                else {
                    txn.rollback();
                    LOG.warning(PERMISSION_DENIED);
                    return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
                }
            }

            data.fillGaps(nucleus);
            Entity newNucleus = Entity.newBuilder(nucleus)
                    .set("name", data.newName)
                    .set("id", data.id)
                    .set("president", data.president)
                    .set("email", data.nucleusEmail)
                    .set("website", data.website)
                    .set("instagram", data.instagram)
                    .set("twitter", data.twitter)
                    .set("facebook", data.facebook)
                    .set("youtube", data.youtube)
                    .set("linkedIn", data.linkedIn)
                    .set("description", data.description)
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.update(newNucleus);
            txn.commit();

            LOG.info("Nucleus " + data.name + " has been edited.");
            return Response.ok(newNucleus).entity("Nucleus edited successfully").build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/delete")
    public Response deleteNucleus(@HeaderParam("Authorization") String token,
                                  @QueryParam("id") String id){
        LOG.fine("Attempt to delete nucleus.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(id);
            Entity nucleus = txn.get(nucleusKey);

            if(!String.valueOf(getRole(decodedToken)).replaceAll("\"", "").equals(BO)){
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }
            if( nucleus == null ) {
                txn.rollback();
                LOG.warning(NUCLEUS_DOES_NOT_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_DOES_NOT_EXISTS).build();
            }

            String list = nucleus.getString("members_list");
            String userPersonalList;
            Key memberKey;
            Entity memberEntity;
            Entity newUser;
            for(String member : list.split("#")) {
                if(!member.equals("")) {
                    memberKey = datastore.newKeyFactory().setKind(USER).newKey(member);
                    memberEntity = txn.get(memberKey);

                    if (memberEntity == null) {
                        txn.rollback();
                        LOG.warning(WRONG_MEMBER);
                        return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_MEMBER).build();
                    }

                    userPersonalList = memberEntity.getString("job_list");
                    userPersonalList = userPersonalList.replace("#" + nucleus.getString("id") + "%member", "");
                    newUser = Entity.newBuilder(memberEntity)
                            .set("job_list", userPersonalList)
                            .set("time_lastupdate", Timestamp.now())
                            .build();
                    txn.update(newUser);
                }
            }
            txn.delete(nucleusKey);
            txn.commit();

            LOG.info("Nucleus deleted.");
            return Response.ok(nucleusKey).build();
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
                                 @QueryParam("offset") String offset,
                                 Map<String, String> filters){
        LOG.fine("Attempt to query nucleus.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
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
                .setKind("Nucleus")
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
    @Path("/add/members/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addMembers(@HeaderParam("Authorization") String token,
                               @PathParam("id") String id,
                               NucleusData data) {
        LOG.fine("Attempt to add members to the nucleus.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(id);
            Entity nucleus = txn.get(nucleusKey);

            if( nucleus == null ) {
                txn.rollback();
                LOG.warning(NUCLEUS_DOES_NOT_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_DOES_NOT_EXISTS).build();
            }
            if(!getRole(decodedToken).equals(BO) && !nucleus.getString("president").equals(decodedToken.getUid())) {
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }

            String list = nucleus.getString("members_list");;
            String userPersonalList;
            Key memberKey;
            Entity memberEntity;
            Entity newUser;

            for(String member : data.members) {
                memberKey = datastore.newKeyFactory().setKind(USER).newKey(member);
                memberEntity = txn.get(memberKey);

                if(memberEntity == null){
                    txn.rollback();
                    LOG.warning(WRONG_MEMBER);
                    return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_MEMBER).build();
                }

                if (!list.contains(member)){
                    userPersonalList = memberEntity.getString("job_list");
                    userPersonalList = userPersonalList.concat("#" + nucleus.getString("id") + "%" + "member");
                    newUser = Entity.newBuilder(memberEntity)
                            .set("job_list", userPersonalList)
                            .set("time_lastupdate", Timestamp.now())
                            .build();

                    txn.update(newUser);
                    list = list.concat("#" + member);
                }
            }
            Entity updatedNucleus = Entity.newBuilder(nucleus)
                    .set("members_list", list)
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.update(updatedNucleus);
            txn.commit();

            LOG.info("Members added.");
            return Response.ok(updatedNucleus).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


    @PATCH
    @Path("/delete/members/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteMembers(@HeaderParam("Authorization") String token,
                                  @PathParam("id") String id,
                                  NucleusData data) {
        LOG.fine("Attempt to add members to the nucleus.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if (data.validateList()) {
            LOG.warning("List is empty.");
            return Response.status(Response.Status.BAD_REQUEST).entity("List is empty").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(id);
            Entity nucleus = txn.get(nucleusKey);

            if (nucleus == null) {
                txn.rollback();
                LOG.warning(NUCLEUS_DOES_NOT_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_DOES_NOT_EXISTS).build();
            }
            if (!getRole(decodedToken).equals(BO) && !nucleus.getString("president").equals(decodedToken.getUid())) {
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }

            String list = nucleus.getString("members_list");
            String userPersonalList;
            Key memberKey;
            Entity memberEntity;
            Entity newUser;

            for(String member : data.members) {
                memberKey = datastore.newKeyFactory().setKind(USER).newKey(member);
                memberEntity = txn.get(memberKey);

                if(memberEntity == null){
                    txn.rollback();
                    LOG.warning(WRONG_MEMBER);
                    return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_MEMBER).build();
                }

                userPersonalList = memberEntity.getString("job_list");
                userPersonalList = userPersonalList.replace("#" + nucleus.getString("id") + "%member", "");
                newUser = Entity.newBuilder(memberEntity)
                        .set("job_list", userPersonalList)
                        .set("time_lastupdate", Timestamp.now())
                        .build();

                txn.update(newUser);
                list = list.replace("#"+member, "");
            }
            Entity updatedNucleus = Entity.newBuilder(nucleus)
                    .set("members_list", list)
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.update(updatedNucleus);
            txn.commit();

            LOG.info("Members removed.");
            return Response.ok(updatedNucleus).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

}