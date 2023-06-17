package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.*;
import util.DepartmentData;
import util.NucleusData;
import util.ValToken;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/nucleus")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class NucleusResource {
    private static final Logger LOG = Logger.getLogger(NucleusResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public NucleusResource() { }



    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@Context HttpServletRequest request, NucleusData data) {
        LOG.fine("Attempt to create a nucleus by: " + data.president);

        if( !data.validateRegister() ) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.FORBIDDEN).entity("Token not found").build();
            }

            Key presidentKey = datastore.newKeyFactory().setKind("User").newKey(data.president);
            Entity president = txn.get(presidentKey);

            if (president == null){
                txn.rollback();
                LOG.warning("President does not exist");
                return Response.status(Response.Status.BAD_REQUEST).entity("Nucleus already exists").build();
            }

            String creatorUsername = String.valueOf(token.getClaim("user")).replaceAll("\"", "");

            String creatorRole = String.valueOf(token.getClaim("role")).replaceAll("\"", "");


            if (!creatorRole.equals("BO")) {
                if (creatorRole.equals("A")) {
                    Key studentsUnionKey = datastore.newKeyFactory().setKind("Students Union").newKey("Students Union");
                    Entity studentsUnion = txn.get(studentsUnionKey);

                    if (studentsUnion == null){
                        txn.rollback();
                        LOG.warning("Entity does not exist");
                        return Response.status(Response.Status.FORBIDDEN).entity("Permission denied to create a Nucleus").build();
                    }
                    else if (!studentsUnion.getString("president").equals(creatorUsername)){
                        txn.rollback();
                        LOG.warning("Permission denied to create a Nucleus");
                        return Response.status(Response.Status.FORBIDDEN).entity("Permission denied to create a Nucleus").build();
                    }
                }
                else{
                    txn.rollback();
                    LOG.warning("Permission denied to create a Nucleus");
                    return Response.status(Response.Status.FORBIDDEN).entity("Permission denied to create a Nucleus").build();
                }
            }

            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(data.name);
            Entity nucleus = txn.get(nucleusKey);

            if( nucleus != null ) {
                txn.rollback();
                LOG.warning("Nucleus already exists");
                return Response.status(Response.Status.BAD_REQUEST).entity("Nucleus already exists").build();
            } else {

                nucleus = Entity.newBuilder(nucleusKey)
                        .set("email", data.nucleusEmail)
                        .set("name", data.name)
                        .set("id", data.id)
                        .set("president", data.president)
                        .set("members_list", "")
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
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.FORBIDDEN).entity("Token not found").build();
            }

            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(data.name);
            Entity nucleus = txn.get(nucleusKey);

            if( nucleus == null ) {
                txn.rollback();
                LOG.warning("Nucleus does not exist.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Nucleus does not exist.").build();
            }

            String modifierUsername = String.valueOf(token.getClaim("user")).replaceAll("\"", "");
            String modifierRole = String.valueOf(token.getClaim("role")).replaceAll("\"", "");

            if (!modifierRole.equals("BO")) {
                if (modifierRole.equals("A")) {
                    Key studentsUnionKey = datastore.newKeyFactory().setKind("Students Union").newKey("Students Union");
                    Entity studentsUnion = txn.get(studentsUnionKey);

                    if (studentsUnion == null){
                        txn.rollback();
                        LOG.warning("Entity does not exist");
                        return Response.status(Response.Status.NOT_FOUND).entity("Permission denied to create a Nucleus").build();
                    }
                    else if (!studentsUnion.getString("president").equals(modifierUsername) || !nucleus.getString("president").equals(modifierUsername) ){
                        txn.rollback();
                        LOG.warning("Permission denied to create a Nucleus");
                        return Response.status(Response.Status.FORBIDDEN).entity("Permission denied to create a Nucleus").build();
                    }
                }
                else{
                    txn.rollback();
                    LOG.warning("Permission denied to create a Nucleus");
                    return Response.status(Response.Status.FORBIDDEN).entity("Permission denied to create a Nucleus").build();
                }
            }

            data.fillGaps(nucleus);

            Entity newNucleus = Entity.newBuilder(nucleus)
                    .set("name", data.newName)
                    .set("id", data.id)
                    .set("president", data.president)
                    .set("nucleusEmail", data.nucleusEmail)
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
    @Path("/delete/{id}")
    public Response deleteNucleus(@Context HttpServletRequest request, @QueryParam("acronym") String id){
        LOG.fine("Attempt to delete nucleus.");

        Transaction txn = datastore.newTransaction();

        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.FORBIDDEN).entity("Token not found").build();
            }

            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(id);
            Entity nucleus = txn.get(nucleusKey);

            if(!String.valueOf(token.getClaim("role")).replaceAll("\"", "").equals("BO")){
                txn.rollback();
                LOG.warning("Nice try but your not a capi person");
                return Response.status(Response.Status.BAD_REQUEST).entity("Your not one of us\n" +
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
                        "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠙⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀").build();
            }else if( nucleus == null ) {
                txn.rollback();
                LOG.warning("Nucleus does not exist");
                return Response.status(Response.Status.BAD_REQUEST).entity("Nucleus does not exist").build();
            } else {
                String list = nucleus.getString("members_list");
                String userPersonalList;
                Key memberKey;
                Entity memberEntity;
                Entity newUser;
                for(String member : list.split("#")) {
                    if(!member.equals("")) {

                        memberKey = datastore.newKeyFactory().setKind("User").newKey(member);
                        memberEntity = txn.get(memberKey);
                        if (memberEntity == null) {
                            txn.rollback();
                            LOG.warning("Member doesn't exists.");
                            return Response.status(Response.Status.BAD_REQUEST).entity("Member doesn't exists.").build();
                        }
                        userPersonalList = memberEntity.getString("job_list");
                        userPersonalList = userPersonalList.replace("#" + nucleus.getString("id") + "-member", "");
                        newUser = Entity.newBuilder(memberEntity)
                                .set("job_list", userPersonalList)
                                .set("time_lastupdate", Timestamp.now())
                                .build();

                        txn.update(newUser);
                    }
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
    @Path("/add/members/{id}")
    @Consumes(MediaType.APPLICATION_JSON)                                        //list composta por string que tem valor: "papel-username"
    public Response addMembers(@Context HttpServletRequest request, @PathParam("id") String id, NucleusData data) {
        LOG.fine("Attempt to add members to the nucleus.");

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.FORBIDDEN).entity("Token not found").build();
            }

            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(id);
            Entity nucleus = txn.get(nucleusKey);

            if( nucleus == null ) {
                txn.rollback();
                LOG.warning("Nucleus does not exist.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Nucleus does not exist.").build();
            }
            else if(!String.valueOf(token.getClaim("role")).replaceAll("\"", "").equals("BO") && !nucleus.getString("president").equals(String.valueOf(token.getClaim("user")).replaceAll("\"", ""))){
                txn.rollback();
                LOG.warning("Nice try but your not a capi person");
                return Response.status(Response.Status.BAD_REQUEST).entity("Your not one of us\n" +
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
                        "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠙⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀").build();
            }

            String list = nucleus.getString("members_list");;
            String userPersonalList;
            Key memberKey;
            Entity memberEntity;
            Entity newUser;

            for(String member : data.members) {

                memberKey = datastore.newKeyFactory().setKind("User").newKey(member);
                memberEntity = txn.get(memberKey);
                if(memberEntity == null){
                    txn.rollback();
                    LOG.warning("Member doesn't exists.");
                    return Response.status(Response.Status.BAD_REQUEST).entity("Member doesn't exists.").build();
                }
                if (!list.contains(member)){
                    userPersonalList = memberEntity.getString("job_list");
                    userPersonalList = userPersonalList.concat("#" + nucleus.getString("id") + "-" + "member");
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
                LOG.warning("Token not found");
                return Response.status(Response.Status.FORBIDDEN).entity("Token not found").build();
            }

            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(id);
            Entity nucleus = txn.get(nucleusKey);
            if (!String.valueOf(token.getClaim("role")).replaceAll("\"", "").equals("BO") && !nucleus.getString("president").equals(String.valueOf(token.getClaim("user")).replaceAll("\"", ""))) {
                txn.rollback();
                LOG.warning("Nice try but your not a capi person");
                return Response.status(Response.Status.BAD_REQUEST).entity("Your not one of us\n" +
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
                        "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠙⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀").build();
            } else if (nucleus == null) {
                txn.rollback();
                LOG.warning("Nucleus does not exist.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Nucleus does not exist.").build();
            }

            String list = nucleus.getString("members_list");
            String userPersonalList;
            Key memberKey;
            Entity memberEntity;
            Entity newUser;
            for(String member : data.members) {
                memberKey = datastore.newKeyFactory().setKind("User").newKey(member);
                memberEntity = txn.get(memberKey);
                if(memberEntity == null){
                    txn.rollback();
                    LOG.warning("Member doesn't exists.");
                    return Response.status(Response.Status.BAD_REQUEST).entity("Member doesn't exists.").build();
                }
                userPersonalList = memberEntity.getString("job_list");
                userPersonalList = userPersonalList.replace("#" + nucleus.getString("id") + "-member", "");
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

}
