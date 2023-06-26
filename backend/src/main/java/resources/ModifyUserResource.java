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

import static util.Constants.*;

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
            DecodedJWT token = AuthToken.validateToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""));
            Entity user = txn.get(userKey);
            data.fillGaps(user);
            if( user == null ) {
                txn.rollback();
                LOG.warning(USER_OR_PASSWORD_INCORRECT);
                return Response.status(Response.Status.BAD_REQUEST).entity("User or password incorrect " + token.getClaim(USER_CLAIM).toString()).build();
            } else {
                Entity newUser = Entity.newBuilder(user)
                        .set("name", data.name)
                        .set("license_plate", data.license_plate)
                        .set("time_lastupdate", Timestamp.now())
                        .build();

                txn.update(newUser);
                LOG.info(token.getClaim(USER_CLAIM).toString() + " edited.");
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
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            DecodedJWT token = AuthToken.validateToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""));
            Entity user = txn.get(userKey);

            if( user == null ) {
                txn.rollback();
                LOG.warning(USER_OR_PASSWORD_INCORRECT);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_OR_PASSWORD_INCORRECT).build();
            } else {
                if(user.getString("password").equals(DigestUtils.sha512Hex(data.password)) ) {

                    Entity newUser = Entity.newBuilder(user)
                            .set("password", DigestUtils.sha512Hex(data.newPwd))
                            .set("time_lastupdate", Timestamp.now())
                            .build();

                    txn.update(newUser);
                    LOG.info(token.getClaim(USER_CLAIM).toString() + " pwd edited.");
                    txn.commit();
                    return Response.ok(user).build();
                } else {
                    txn.rollback();
                    LOG.warning(USER_OR_PASSWORD_INCORRECT);
                    return Response.status(Response.Status.BAD_REQUEST).entity(USER_OR_PASSWORD_INCORRECT).build();
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
            DecodedJWT token = AuthToken.validateToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""));
            Key targetKey = datastore.newKeyFactory().setKind(USER).newKey(data.target);
            Entity user = txn.get(userKey);
            Entity target = txn.get(targetKey);

            //Falta criar token novo e apagar o antigo

            if(user == null || target == null) {
                txn.rollback();
                LOG.warning(ONE_OF_THE_USERS_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(ONE_OF_THE_USERS_DOES_NOT_EXIST).build();
            } else
            if( !data.validatePermission(String.valueOf(token.getClaim(ROLE)).replaceAll("\"", ""), target.getString(ROLE))) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.BAD_REQUEST).entity(PERMISSION_DENIED).build();
            } else {
                Entity.Builder newUser = Entity.newBuilder(target);

                newUser.set("email", target.getString("email"))
                        .set("name", target.getString("name"))
                        .set("role", data.newRole)
                        .set("license_plate", target.getString("license_plate"))
                        .set("job_list",  target.getString("job_list"))
                        .set("personal_event_list", target.getString("personal_event_list"))  //#string%string%string%string#string%...
                        .set("time_creation", target.getTimestamp("time_creation"))
                        .set("time_lastupdate", Timestamp.now());

                if(data.newRole.equals(D)){
                    if(data.office == null)
                        data.office = "";
                    newUser.set("office", data.office);
                }else
                    newUser.set("office", "");
                Entity u = newUser.build();
                txn.put(u);
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
            DecodedJWT token = AuthToken.validateToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""));
            Key targetKey = datastore.newKeyFactory().setKind(USER).newKey(data.target);
            Entity user = txn.get(userKey);
            Entity target = txn.get(targetKey);

            //Falta criar token novo e apagar o antigo

            if(user == null || target == null) {
                txn.rollback();
                LOG.warning(ONE_OF_THE_USERS_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(ONE_OF_THE_USERS_DOES_NOT_EXIST).build();
            } else
            if( !data.validateDelete(String.valueOf(token.getClaim(ROLE)).replaceAll("\"", ""), target.getString(ROLE))) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.BAD_REQUEST).entity(PERMISSION_DENIED).build();
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