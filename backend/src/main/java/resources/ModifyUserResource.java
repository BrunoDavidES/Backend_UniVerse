package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.cloud.firestore.FieldValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import models.ModifyAttributesData;
import models.ModifyPwdData;
import models.ModifyRoleData;
import org.apache.commons.codec.digest.DigestUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.FirebaseAuth.authenticateToken;
import static utils.FirebaseAuth.getRole;

/**
 * Resource class for modifying user attributes and roles.
 */
@Path("/modify")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyUserResource {
    private static final Logger LOG = Logger.getLogger(ModifyUserResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    /**
     * Modifies the attributes of a user.
     *
     * @param token The authorization token.
     * @param data  The data containing the modified attributes.
     * @return The response indicating the success or failure of the modification.
     * It will return 401 error if the token doesn't exist.
     * It will return 400 error if the user or password are incorrect,
     * if the department doesn't exist
     * or if the nucleus doesn't exist.
     */
    @POST
    @Path("/attributes")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyAttributes(@HeaderParam("Authorization") String token, ModifyAttributesData data) {
        LOG.fine("Attempt to modify user.");

        FirebaseToken decodedToken = authenticateToken(token);
        if (decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        return modifyAttributesValidation(decodedToken, data);
    }

    /**
     * Modifies the role, department, department_job, of a user.
     *
     * @param token The authorization token.
     * @param data  The data containing the target user and the new role.
     * @return The response indicating the success or failure of the modification.
     * It will return 401 error if the token doesn't exist.
     * It will return 400 error if the user or the target doesn't exist,
     * if the user does not have the necessary permission (admin changes all except admin, backoffice changes all except admin and backoffice)
     * or department doesn't exist.
     */
    @POST
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyRole(@HeaderParam("Authorization") String token, ModifyRoleData data) {
        LOG.fine("Attempt to modify role of: " + data.getTarget() + " to " + data.getNewRole() + ".");

        FirebaseToken decodedToken = authenticateToken(token);
        if (decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        return modifyRoleAndDepartmentValidation(decodedToken, data);
    }


    /**
     * Deletes a user.
     *
     * @param token The authorization token.
     * @param data  The data containing the target user to be deleted.
     * @return The response indicating the success or failure of the deletion.
     * It will return 401 error if the token doesn't exist.
     * It will return 400 error if the user or the target doesn't exist
     * or if the user does not have the necessary permission (admin deletes all, backoffice deletes all except admin and backoffice).
     */
    @DELETE
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam("Authorization") String token, ModifyRoleData data) {
        LOG.fine("Attempt to delete: " + data.getTarget() + ".");

        FirebaseToken decodedToken = authenticateToken(token);
        if (decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        return deleteUserValidation(decodedToken, data);
    }

    private Response modifyAttributesValidation(FirebaseToken decodedToken, ModifyAttributesData data){
        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
            Entity user = txn.get(userKey);
            data.fillGaps(user);
            if (!data.getDepartment().equals("")) {
                Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.getDepartment());
                Entity department = txn.get(departmentKey);
                if (department == null) {
                    txn.rollback();
                    LOG.warning(WRONG_DEPARTMENT);
                    return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
                }
            }
            if (!data.getNucleus().equals("")) {
                Key nucleustKey = datastore.newKeyFactory().setKind("Nucleus").newKey(data.getNucleus());
                Entity nucleus = txn.get(nucleustKey);
                if (nucleus == null) {
                    txn.rollback();
                    LOG.warning(NUCLEUS_DOES_NOT_EXISTS);
                    return Response.status(Response.Status.BAD_REQUEST).entity(NUCLEUS_DOES_NOT_EXISTS).build();
                }
            }
            if (user == null) {
                txn.rollback();
                LOG.warning(USER_OR_PASSWORD_INCORRECT);
                return Response.status(Response.Status.BAD_REQUEST).entity("User or password incorrect " + decodedToken.getUid()).build();
            } else {
                return modifyUserAttributes(decodedToken, data, txn, user);
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    private Response modifyUserAttributes(FirebaseToken decodedToken, ModifyAttributesData data, Transaction txn, Entity user){
        Entity newUser = Entity.newBuilder(user)
                .set("name", data.getName())
                .set("phone", data.getPhone())
                .set("status", data.getStatus())
                .set("privacy", data.getPrivacy())
                .set("nucleus", data.getNucleus())
                .set("license_plate", data.getLicense_plate())
                .set("office", data.getOffice())
                .set("linkedin", data.getLinkedin())
                .set("time_lastupdate", Timestamp.now())
                .build();

        txn.update(newUser);
        LOG.info(decodedToken.getUid() + " edited.");
        txn.commit();
        return Response.ok(user).build();
    }

    private Response modifyRoleAndDepartmentValidation(FirebaseToken decodedToken, ModifyRoleData data){
        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
            Key targetKey = datastore.newKeyFactory().setKind(USER).newKey(data.getTarget());
            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.getDepartment());
            Entity user = txn.get(userKey);
            Entity target = txn.get(targetKey);
            Entity department = txn.get(departmentKey);

            //Falta criar token novo e apagar o antigo

            if (user == null || target == null) {
                txn.rollback();
                LOG.warning(ONE_OF_THE_USERS_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(ONE_OF_THE_USERS_DOES_NOT_EXIST).build();
            } else if (!data.validatePermission(getRole(decodedToken), getRole(firebaseAuth.getUser(data.getTarget())), target)) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.BAD_REQUEST).entity(PERMISSION_DENIED).build();
            } else {
                if (!data.validatePermission(getRole(decodedToken), getRole(firebaseAuth.getUser(data.getTarget())), target)) {
                    txn.rollback();
                    LOG.warning(PERMISSION_DENIED);
                    return Response.status(Response.Status.BAD_REQUEST).entity(PERMISSION_DENIED).build();
                } else if (department == null) {
                    txn.rollback();
                    LOG.warning(WRONG_DEPARTMENT);
                    return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
                } else {
                    return modifyRoleAndDepartment(data, txn, target);
                }
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    private Response modifyRoleAndDepartment(ModifyRoleData data, Transaction txn, Entity target) throws FirebaseAuthException {
        Entity.Builder newUser = Entity.newBuilder(target);
        if (data.getNewRole() != null && !data.getNewRole().equals("")) {
            Map<String, Object> customClaims = new HashMap<>();
            customClaims.put(ROLE, data.getNewRole());
            customClaims.put(LAST_UPDATE, Timestamp.now());
            firebaseAuth.setCustomUserClaims(data.getTarget(), customClaims);
            LOG.info("role: " + data.getNewRole() + ", " + getRole(firebaseAuth.getUser(data.getTarget())));
        }
        newUser.set("department", data.getDepartment())
                .set("department_job", data.department_job);

        if (data.getNewRole().equals(TEACHER)) {
            if (data.getOffice() == null)
                data.setOffice("");
            newUser.set("nucleus", "")
                    .set("nucleus_job", "")
                    .set("office", data.getOffice());
        } else
            newUser.set("office", "");
        Entity u = newUser.build();
        txn.update(u);
        LOG.info(data.getTarget() + " role has been updated successfully.");
        txn.commit();
        return Response.ok(target).build();
    }

    private Response deleteUserValidation(FirebaseToken decodedToken, ModifyRoleData data){
        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
            Key targetKey = datastore.newKeyFactory().setKind(USER).newKey(data.getTarget());
            Entity user = txn.get(userKey);
            Entity target = txn.get(targetKey);

            //Falta criar token novo e apagar o antigo

            if (user == null || target == null) {
                txn.rollback();
                LOG.warning(ONE_OF_THE_USERS_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(ONE_OF_THE_USERS_DOES_NOT_EXIST).build();
            } else if (!data.validateDelete(getRole(decodedToken), getRole(firebaseAuth.getUser(data.getTarget())))
                    && !decodedToken.getUid().equals(data.getTarget())) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.BAD_REQUEST).entity(PERMISSION_DENIED).build();
            } else {
                return deleteUser(txn, data, targetKey, target);
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    private Response deleteUser(Transaction txn, ModifyRoleData data, Key targetKey, Entity target) throws FirebaseAuthException {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("PersonalEvent")
                .setFilter(StructuredQuery.PropertyFilter.hasAncestor(targetKey))
                .build();

        QueryResults<Entity> queryResults = datastore.run(query);
        Entity memberEntity;
        while (queryResults.hasNext()) {
            memberEntity = queryResults.next();
            txn.delete(memberEntity.getKey());
        }
        txn.delete(targetKey);
        firebaseAuth.deleteUser(data.getTarget());
        LOG.info("Target deleted.");
        txn.commit();
        return Response.ok(target).build();
    }
}
