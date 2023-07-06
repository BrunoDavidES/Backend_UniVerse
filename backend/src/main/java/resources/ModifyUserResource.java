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

@Path("/modify")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyUserResource {
    private static final Logger LOG = Logger.getLogger(ModifyUserResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

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

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
            Entity user = txn.get(userKey);
            data.fillGaps(user);
            if (!data.department.equals("")) {
                Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.department);
                Entity department = txn.get(departmentKey);
                if (department == null) {
                    txn.rollback();
                    LOG.warning(WRONG_DEPARTMENT);
                    return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
                }
            }
            if (!data.nucleus.equals("")) {
                Key nucleustKey = datastore.newKeyFactory().setKind("Nucleus").newKey(data.nucleus);
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
                Entity newUser = Entity.newBuilder(user)
                        .set("name", data.name)
                        .set("status", data.status)
                        .set("nucleus", data.nucleus)
                        .set("license_plate", data.license_plate)
                        .set("time_lastupdate", Timestamp.now())
                        .build();

                txn.update(newUser);
                LOG.info(decodedToken.getUid() + " edited.");
                txn.commit();
                return Response.ok(user).build();

            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
/*
    @POST
    @Path("/pwd")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyPwd(@HeaderParam("Authorization") String token, ModifyPwdData data) {
        LOG.fine("Attempt to modify pwd.");

        FirebaseToken decodedToken = authenticateToken(token);
        if (decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        if (!data.validatePwd()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
            Entity user = txn.get(userKey);

            if (user == null) {
                txn.rollback();
                LOG.warning(USER_OR_PASSWORD_INCORRECT);
                return Response.status(Response.Status.BAD_REQUEST).entity(USER_OR_PASSWORD_INCORRECT).build();
            } else {
                if (user.getString("password").equals(DigestUtils.sha512Hex(data.password))) {

                    Entity newUser = Entity.newBuilder(user)
                            .set("password", DigestUtils.sha512Hex(data.newPwd))
                            .set("time_lastupdate", Timestamp.now())
                            .build();

                    txn.update(newUser);
                    LOG.info(decodedToken.getUid() + " pwd edited.");
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
*/
    @POST
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyRole(@HeaderParam("Authorization") String token, ModifyRoleData data) {
        LOG.fine("Attempt to modify role of: " + data.target + " to " + data.newRole + ".");

        FirebaseToken decodedToken = authenticateToken(token);
        if (decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
            Key targetKey = datastore.newKeyFactory().setKind(USER).newKey(data.target);
            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.department);
            Entity user = txn.get(userKey);
            Entity target = txn.get(targetKey);
            Entity department = txn.get(departmentKey);

            //Falta criar token novo e apagar o antigo

            if (user == null || target == null) {
                txn.rollback();
                LOG.warning(ONE_OF_THE_USERS_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(ONE_OF_THE_USERS_DOES_NOT_EXIST).build();
            } else if (!data.validatePermission(getRole(decodedToken), getRole(firebaseAuth.getUser(data.target)), target)) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.BAD_REQUEST).entity(PERMISSION_DENIED).build();
            } else {
                if (!data.validatePermission(getRole(decodedToken), getRole(firebaseAuth.getUser(data.target)), target)) {
                    txn.rollback();
                    LOG.warning(PERMISSION_DENIED);
                    return Response.status(Response.Status.BAD_REQUEST).entity(PERMISSION_DENIED).build();
                } else if (department == null) {
                    txn.rollback();
                    LOG.warning(WRONG_DEPARTMENT);
                    return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
                } else {
                        Entity.Builder newUser = Entity.newBuilder(target);
                    if(data.newRole != null && !data.newRole.equals("")) {
                        Map<String, Object> customClaims = new HashMap<>();
                        customClaims.put(ROLE, data.newRole);
                        customClaims.put(LAST_UPDATE, Timestamp.now());
                        firebaseAuth.setCustomUserClaims(data.target, customClaims);
                        LOG.info("role: "+data.newRole+", " + getRole(firebaseAuth.getUser(data.target)));
                    }
                    newUser.set("department", data.department)
                            .set("department_job", data.department_job);

                    if (data.newRole.equals(TEACHER)) {
                        if (data.office == null)
                            data.office = "";
                        newUser.set("nucleus", "")
                                .set("nucleus_job", "")
                                .set("office", data.office);
                    } else
                        newUser.set("office", "");
                    Entity u = newUser.build();
                    txn.update(u);
                    LOG.info(data.target + " role has been updated successfully.");
                    txn.commit();
                    return Response.ok(target).build();
                }
            }
            } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        } finally{
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        }

        @DELETE
        @Path("/delete")
        @Consumes(MediaType.APPLICATION_JSON)
        public Response deleteUser (@HeaderParam("Authorization") String token, ModifyRoleData data){
            LOG.fine("Attempt to delete: " + data.target + ".");

            FirebaseToken decodedToken = authenticateToken(token);
            if (decodedToken == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Transaction txn = datastore.newTransaction();
            try {
                Key userKey = datastore.newKeyFactory().setKind(USER).newKey(decodedToken.getUid());
                Key targetKey = datastore.newKeyFactory().setKind(USER).newKey(data.target);
                Entity user = txn.get(userKey);
                Entity target = txn.get(targetKey);

                //Falta criar token novo e apagar o antigo

                if (user == null || target == null) {
                    txn.rollback();
                    LOG.warning(ONE_OF_THE_USERS_DOES_NOT_EXIST);
                    return Response.status(Response.Status.BAD_REQUEST).entity(ONE_OF_THE_USERS_DOES_NOT_EXIST).build();
                } else if (!data.validateDelete(getRole(decodedToken), getRole(firebaseAuth.getUser(data.target)))
                        && !decodedToken.getUid().equals(data.target)) {
                    txn.rollback();
                    LOG.warning(PERMISSION_DENIED);
                    return Response.status(Response.Status.BAD_REQUEST).entity(PERMISSION_DENIED).build();
                } else {
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
                    firebaseAuth.deleteUser(data.target);
                    LOG.info("Target deleted.");
                    txn.commit();
                    return Response.ok(target).build();
                }
            } catch (FirebaseAuthException e) {
                throw new RuntimeException(e);
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        }
}
