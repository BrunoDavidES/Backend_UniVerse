package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import util.DepartmentData;
import util.NucleusData;
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

@Path("/nucleus")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class NucleusResource {

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
    private static final Logger LOG = Logger.getLogger(NucleusResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public NucleusResource() { }



    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@Context HttpServletRequest request, NucleusData data) {
        LOG.fine("Attempt to create a nucleus by: " + data.president);

        if( !data.validateRegister() ) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key presidentKey = datastore.newKeyFactory().setKind(USER).newKey(data.president);
            Entity president = txn.get(presidentKey);

            if (president == null){
                txn.rollback();
                LOG.warning(WRONG_PRESIDENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PRESIDENT).build();
            }

            String creatorUsername = String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", "");

            String creatorRole = String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "");


            if (!creatorRole.equals(BO)) {
                if (creatorRole.equals(A)) {
                    Key studentsUnionKey = datastore.newKeyFactory().setKind(STUDENTS_UNION).newKey(STUDENTS_UNION);
                    Entity studentsUnion = txn.get(studentsUnionKey);

                    if (studentsUnion == null){
                        txn.rollback();
                        LOG.warning(ENTITY_DOES_NOT_EXIST);
                        return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
                    }
                    else if (!studentsUnion.getString("president").equals(creatorUsername)){
                        txn.rollback();
                        LOG.warning(PERMISSION_DENIED);
                        return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
                    }
                }
                else{
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
            } else {

                nucleus = Entity.newBuilder(nucleusKey)
                        .set("email", data.nucleusEmail)
                        .set("name", data.name)
                        .set("id", data.id)
                        .set("location", data.location)
                        .set("president", data.president)
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

                LOG.info("Nucleus registered: " + data.id + "| " + data.name);
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
    public Response modifyNucleus(@Context HttpServletRequest request, NucleusData data){
        LOG.fine("Attempt to modify nucleus.");

        if( !data.validateModify()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(data.id);
            Entity nucleus = txn.get(nucleusKey);

            if( nucleus == null ) {
                txn.rollback();
                LOG.warning(NUCLEUS_DOES_NOT_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_DOES_NOT_EXISTS).build();
            }

            String modifierUsername = String.valueOf(token.getClaim(USER)).replaceAll("\"", "");
            String modifierRole = String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "");

            if (!modifierRole.equals(BO)) {
                if (modifierRole.equals(A)) {
                    Key studentsUnionKey = datastore.newKeyFactory().setKind(STUDENTS_UNION).newKey(STUDENTS_UNION);
                    Entity studentsUnion = txn.get(studentsUnionKey);

                    if (studentsUnion == null){
                        txn.rollback();
                        LOG.warning(ENTITY_DOES_NOT_EXIST);
                        return Response.status(Response.Status.NOT_FOUND).entity(PERMISSION_DENIED).build();
                    }
                    else if (!studentsUnion.getString("president").equals(modifierUsername) || !nucleus.getString("president").equals(modifierUsername) ){
                        txn.rollback();
                        LOG.warning(PERMISSION_DENIED);
                        return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
                    }
                }
                else{
                    txn.rollback();
                    LOG.warning(PERMISSION_DENIED);
                    return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
                }
            }

            data.fillGaps(nucleus);

            Entity newNucleus = Entity.newBuilder(nucleus)
                    .set("name", data.newName)
                    .set("id", data.id)
                    .set("location", data.location)
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

            LOG.info("Nucleus " + data.name + " has been edited.");
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
    public Response deleteNucleus(@Context HttpServletRequest request, @QueryParam("id") String id){
        LOG.fine("Attempt to delete nucleus.");

        Transaction txn = datastore.newTransaction();

        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(id);
            Entity nucleus = txn.get(nucleusKey);

            if(!String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "").equals(BO)){
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
    public Response queryNucleus(@Context HttpServletRequest request,
                                 @QueryParam("limit") String limit,
                                 @QueryParam("offset") String cursor, Map<String, String> filters){
        LOG.fine("Attempt to query nucleus.");

        //Verificar, caso for evento privado, se o token é valido
        final ValToken validator = new ValToken();
        DecodedJWT token = validator.checkToken(request);

        if (token == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
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
                .setKind("Nucleus")
                .setFilter(attributeFilter)
                .setLimit(Integer.parseInt(limit));

        if ( !cursor.equals("EMPTY") ){
            query.setStartCursor(Cursor.fromUrlSafe(cursor));
        }

        queryResults = datastore.run(query.build());

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        LOG.info("Ides receber um query ó filho!");
        Gson g = new Gson();

        return Response.ok(g.toJson(results))
                .header("X-Cursor",queryResults.getCursorAfter().toUrlSafe())
                .build();
    }
/*
    @POST
    @Path("/add/members/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addMembers(@Context HttpServletRequest request, @PathParam("id") String id, NucleusData data) {
        LOG.fine("Attempt to add members to the nucleus.");

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(id);
            Entity nucleus = txn.get(nucleusKey);

            if( nucleus == null ) {
                txn.rollback();
                LOG.warning(NUCLEUS_DOES_NOT_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_DOES_NOT_EXISTS).build();
            }
            else if(!String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "").equals(BO) && !nucleus.getString("president").equals(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""))){
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
            LOG.info("Members added.");
            txn.commit();
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
    public Response deleteMembers(@Context HttpServletRequest request, @PathParam("id") String id, NucleusData data) {
        LOG.fine("Attempt to add members to the nucleus.");

        if (data.validateList()) {
            LOG.warning("List is empty.");
            return Response.status(Response.Status.BAD_REQUEST).entity("List is empty").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(id);
            Entity nucleus = txn.get(nucleusKey);
            if (!String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "").equals(BO) && !nucleus.getString("president").equals(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""))) {
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            } else if (nucleus == null) {
                txn.rollback();
                LOG.warning(NUCLEUS_DOES_NOT_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_DOES_NOT_EXISTS).build();
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
            LOG.info("Members removed.");
            txn.commit();
            return Response.ok(updatedNucleus).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

 */

}