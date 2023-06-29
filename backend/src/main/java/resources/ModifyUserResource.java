package resources;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.*;
import models.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static utils.FirebaseAuth.*;
import static utils.Constants.*;

@Path("/modify")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyUserResource {
    private static final Logger LOG = Logger.getLogger(ModifyUserResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public ModifyUserResource() {}

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
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
            Entity user = txn.get(userKey);

            if( user == null ) {
                txn.rollback();
                LOG.warning(USER_OR_PASSWORD_INCORRECT);
                return Response.status(Response.Status.BAD_REQUEST).entity("User or password incorrect " + decodedToken.getUid().toString()).build();
            }

            data.fillGaps(user);
            Entity newUser = Entity.newBuilder(user)
                    .set("name", data.name)
                    .set("status", data.status)
                    .set("license_plate", data.license_plate)
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.update(newUser);
            txn.commit();

            LOG.info(decodedToken.getUid() + " edited.");
            return Response.ok(user).build();
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
            UserRecord target = firebaseAuth.getUser(data.target);

            if (!data.validatePermission(getRole(decodedToken), getRole(target))) {
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
            UserRecord target = firebaseAuth.getUser(data.target);
            Key targetKey = datastore.newKeyFactory().setKind(USER).newKey(data.target);

            if( !data.validateDelete(getRole(decodedToken), getRole(target)) ) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.BAD_REQUEST).entity(PERMISSION_DENIED).build();
            }

            firebaseAuth.deleteUser(target.getUid());

            txn.delete(targetKey);
            txn.commit();

            LOG.info("Target deleted.");
            return Response.ok(target).build();
        } catch (FirebaseAuthException e) {
            txn.rollback();
            throw new RuntimeException(e);
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


}
