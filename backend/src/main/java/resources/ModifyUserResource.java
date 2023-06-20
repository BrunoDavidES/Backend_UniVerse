package resources;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/modify")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyUserResource {
    private static final Logger LOG = Logger.getLogger(ModifyUserResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();



    @POST
    @Path("/attributes")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyAttributes(@Context HttpServletRequest request, ModifyAttributesData data){
        LOG.fine("Attempt to modify user.");

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.FORBIDDEN).entity("Token not found").build();
            }
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(String.valueOf(token.getClaim("user")).replaceAll("\"", ""));
            Entity user = txn.get(userKey);
            data.fillGaps(user);
            if( user == null ) {
                txn.rollback();
                LOG.warning("User or password incorrect");
                return Response.status(Response.Status.BAD_REQUEST).entity("User or password incorrect " + token.getClaim("user").toString()).build();
            } else {
                    Entity newUser = Entity.newBuilder(user)
                            .set("name", data.name)
                            .set("status", data.status)
                            .set("time_lastupdate", Timestamp.now())
                            .build();

                    txn.update(newUser);
                    LOG.info(token.getClaim("user").toString() + " edited.");
                    txn.commit();
                    return Response.ok(user).build();

            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/pwd")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyPwd(@Context HttpServletRequest request, ModifyPwdData data){
        LOG.fine("Attempt to modify pwd.");

        if( !data.validatePwd()) {
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
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(String.valueOf(token.getClaim("user")).replaceAll("\"", ""));
            Entity user = txn.get(userKey);

            if( user == null ) {
                txn.rollback();
                LOG.warning("User or password incorrect");
                return Response.status(Response.Status.BAD_REQUEST).entity("User or password incorrect").build();
            } else {
                if(user.getString("password").equals(DigestUtils.sha512Hex(data.password)) ) {

                    Entity newUser = Entity.newBuilder(user)
                            .set("password", DigestUtils.sha512Hex(data.newPwd))
                            .set("time_lastupdate", Timestamp.now())
                            .build();

                    txn.update(newUser);
                    LOG.info(token.getClaim("user").toString() + " pwd edited.");
                    txn.commit();
                    return Response.ok(user).build();
                } else {
                    txn.rollback();
                    LOG.warning("User or password incorrect");
                    return Response.status(Response.Status.BAD_REQUEST).entity("User or password incorrect").build();
                }
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyRole(@Context HttpServletRequest request, ModifyRoleData data){
        LOG.fine("Attempt to modify role of: " + data.target + " to " + data.newRole + ".");
        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.FORBIDDEN).entity("Token not found").build();
            }
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(String.valueOf(token.getClaim("user")).replaceAll("\"", ""));
            Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.target);
            Entity user = txn.get(userKey);
            Entity target = txn.get(targetKey);

            //Falta criar token novo e apagar o antigo

            if(user == null || target == null) {
                txn.rollback();
                LOG.warning("One of the users does not exist.");
                return Response.status(Response.Status.BAD_REQUEST).entity("One of the users does not exist.").build();
            } else
                if( !data.validatePermission(String.valueOf(token.getClaim("role")).replaceAll("\"", ""), target.getString("role"))) {
                    txn.rollback();
                    LOG.warning("Wrong permissions.");
                    return Response.status(Response.Status.BAD_REQUEST).entity("Wrong permissions.").build();
            } else {
                    Entity newUser = Entity.newBuilder(target)
                            .set("role", data.newRole)
                            .set("time_lastupdate", Timestamp.now())
                            .build();

                    txn.update(newUser);
                    LOG.info(data.target + " role has been updated successfully.");
                    txn.commit();
                    return Response.ok(target).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(@Context HttpServletRequest request, ModifyRoleData data){
        LOG.fine("Attempt to delete: " + data.target +".");
        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.FORBIDDEN).entity("Token not found").build();
            }
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(String.valueOf(token.getClaim("user")).replaceAll("\"", ""));
            Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.target);
            Entity user = txn.get(userKey);
            Entity target = txn.get(targetKey);

            //Falta criar token novo e apagar o antigo

            if(user == null || target == null) {
                txn.rollback();
                LOG.warning("One of the users does not exist.");
                return Response.status(Response.Status.BAD_REQUEST).entity("One of the users does not exist.").build();
            } else
            if( !data.validateDelete(String.valueOf(token.getClaim("role")).replaceAll("\"", ""), target.getString("role"))) {
                txn.rollback();
                LOG.warning("Wrong permissions.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Wrong permissions.").build();
            } else {
                txn.delete(targetKey);
                LOG.info("Target deleted.");
                txn.commit();
                return Response.ok(target).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}
