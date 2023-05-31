package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import util.DepartmentData;
import util.NucleusData;
import util.UserData;
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
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
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
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
            }

            Key presidentKey = datastore.newKeyFactory().setKind("User").newKey(data.president);
            Entity president = txn.get(presidentKey);

            if (president == null){
                txn.rollback();
                LOG.warning("President does not exist");
                return Response.status(Response.Status.BAD_REQUEST).entity("Nucleus already exists").build();
            }

            String creatorUsername = String.valueOf(token.getClaim("user"));

            String creatorRole = String.valueOf(token.getClaim("role"));


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
                        .set("acronym", data.acronym)
                        .set("president", data.president)
                        .set("time_creation", Timestamp.now())
                        .set("time_lastupdate", Timestamp.now())
                        .build();
                txn.add(nucleus);

                LOG.info("Nucleus registered: " + data.acronym + "| " + data.name);
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
    public Response modifyDepartment(@Context HttpServletRequest request, NucleusData data){
        LOG.fine("Attempt to modify department.");

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
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
            }

            Key nucleusKey = datastore.newKeyFactory().setKind("Nucleus").newKey(data.name);
            Entity nucleus = txn.get(nucleusKey);

            if( nucleus == null ) {
                txn.rollback();
                LOG.warning("Nucleus does not exist.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Nucleus does not exist.").build();
            }

            data.fillGaps(nucleus);

            String modifierUsername = String.valueOf(token.getClaim("user"));
            String modifierRole = String.valueOf(token.getClaim("role"));

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

            Entity newNucleus = Entity.newBuilder(nucleus)
                    .set("name", data.newName)
                    .set("acronym", data.acronym)
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


}
