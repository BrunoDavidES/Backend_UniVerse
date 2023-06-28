package resources;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.apache.commons.codec.digest.DigestUtils;
import util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static util.AuthToken.*;
import static util.Constants.*;

@Path("/modify")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyUserResource {
    private static final Logger LOG = Logger.getLogger(ModifyUserResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @POST
    @Path("/attributes")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyAttributes(@HeaderParam("Authorization") String token, ModifyAttributesData data){
        LOG.fine("Attempt to modify user.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(decodedToken.getUid()).replaceAll("\"", ""));
            Entity user = txn.get(userKey);
            data.fillGaps(user);
            if( user == null ) {
                txn.rollback();
                LOG.warning(USER_OR_PASSWORD_INCORRECT);
                return Response.status(Response.Status.BAD_REQUEST).entity("User or password incorrect " + decodedToken.getUid().toString()).build();
            } else {
                    Entity newUser = Entity.newBuilder(user)
                            .set("name", data.name)
                            .set("status", data.status)
                            .set("license_plate", data.license_plate)
                            .set("time_lastupdate", Timestamp.now())
                            .build();

                    txn.update(newUser);
                    LOG.info(decodedToken.getUid().toString() + " edited.");
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
    public Response modifyPwd(@HeaderParam("Authorization") String token, ModifyPwdData data){
        LOG.fine("Attempt to modify pwd.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        try {
            String link = firebaseAuth.generatePasswordResetLink(decodedToken.getEmail());
            return Response.ok(link).build();
        } catch (FirebaseAuthException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error generating reset link").build();
        }
    }

    @POST
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyRole(@HeaderParam("Authorization") String token, ModifyRoleData data){
        LOG.fine("Attempt to modify role of: " + data.target + " to " + data.newRole + ".");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(decodedToken.getUid()).replaceAll("\"", ""));
            Key targetKey = datastore.newKeyFactory().setKind(USER).newKey(data.target);
            Entity user = txn.get(userKey);
            Entity target = txn.get(targetKey);

            //Falta criar token novo e apagar o antigo

            if(user == null || target == null) {
                txn.rollback();
                LOG.warning(ONE_OF_THE_USERS_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(ONE_OF_THE_USERS_DOES_NOT_EXIST).build();
            } else
                if( !data.validatePermission(String.valueOf(getRole(decodedToken)).replaceAll("\"", ""), target.getString(ROLE))) {
                    txn.rollback();
                    LOG.warning(PERMISSION_DENIED);
                    return Response.status(Response.Status.BAD_REQUEST).entity(PERMISSION_DENIED).build();
            } else {
                Entity.Builder newUser = Entity.newBuilder(target);

                newUser.set("email", target.getString("email"))
                        .set("name", target.getString("name"))
                        .set("password", target.getString("password"))
                        .set("role", data.newRole)
                        .set("license_plate", target.getString("license_plate"))
                        .set("status",  target.getString("status"))
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
    public Response deleteUser(@HeaderParam("Authorization") String token, ModifyRoleData data){
        LOG.fine("Attempt to delete: " + data.target +".");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(decodedToken.getUid()).replaceAll("\"", ""));
            Key targetKey = datastore.newKeyFactory().setKind(USER).newKey(data.target);
            Entity user = txn.get(userKey);
            Entity target = txn.get(targetKey);

            //Falta criar token novo e apagar o antigo

            if(user == null || target == null) {
                txn.rollback();
                LOG.warning(ONE_OF_THE_USERS_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(ONE_OF_THE_USERS_DOES_NOT_EXIST).build();
            } else
            if( !data.validateDelete(String.valueOf(getRole(decodedToken)).replaceAll("\"", ""), target.getString(ROLE))) {
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
