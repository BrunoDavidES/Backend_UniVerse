package resources;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.*;
import util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static util.FirebaseAuth.*;
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
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyRole(@HeaderParam("Authorization") String token, ModifyRoleData data) {
        LOG.fine("Attempt to modify role of: " + data.target + " to " + data.newRole + ".");

        FirebaseToken decodedToken = authenticateToken(token);
        if (decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        try {
            UserRecord user = firebaseAuth.getUser(decodedToken.getUid());
            UserRecord target = firebaseAuth.getUser(data.target);

            if (!data.validatePermission(getRole(user), getRole(target))) {
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.BAD_REQUEST).entity(PERMISSION_DENIED).build();
            }

            Map<String, Object> customClaims = new HashMap<>();
            customClaims.put(ROLE, data.newRole);
            customClaims.put(LAST_UPDATE, Timestamp.now());

            UpdateRequest updateRequest = new UpdateRequest(target.getUid())
                    .setCustomClaims(customClaims);
            firebaseAuth.updateUser(updateRequest);

            LOG.info(target.getUid() + " role has been updated successfully.");
            return Response.ok(target).build();
        } catch (FirebaseAuthException e) {
            LOG.warning(ONE_OF_THE_USERS_DOES_NOT_EXIST);
            return Response.status(Response.Status.BAD_REQUEST).entity(ONE_OF_THE_USERS_DOES_NOT_EXIST).build();
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
