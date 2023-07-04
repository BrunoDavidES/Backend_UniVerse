package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import models.UserData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static utils.Constants.*;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
    private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public RegisterResource() {}

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(UserData data) throws Exception {
        LOG.fine("Attempt to register user: " + data.username);

        if (!data.validateRegister()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        // Firebase Register
        Response response = firebaseRegister(data.username, data.email, data.password, data.name, data.getRole());
        if(response.getStatus() == Response.Status.OK.getStatusCode()) {

            // Datastore Register
            response = datastoreRegister(data.username, data.email, data.name, data.license_plate);
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                firebaseAuth.deleteUser(data.username);
            }
        }

        return response;
    }

    private Response firebaseRegister(String username, String email, String password, String name, String role) {
        try {
            UserRecord userRecord = firebaseAuth.createUser( new CreateRequest()
                    .setUid(username)
                    .setEmail(email)
                    .setEmailVerified(false)
                    .setPassword(password)
                    .setDisplayName(name)
                    .setDisabled(false)
            );

            Map<String, Object> customClaims = new HashMap<>();
            customClaims.put(ROLE, role);
            customClaims.put(LAST_UPDATE, Timestamp.now());
            firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

            LOG.info("User registered in Firebase: " + userRecord.getUid());
            return Response.ok(userRecord).build();
        } catch (FirebaseAuthException e) {
            LOG.warning("Firebase registration failed: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Firebase registration failed: " + e.getMessage()).build();
        }
    }

    private Response datastoreRegister(String username, String email, String name, String license_plate) {
        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);

            if(license_plate == null)
                license_plate = UNREGISTERED;

            Entity user = Entity.newBuilder(userKey)
                    .set("email", data.email)
                    .set("name", data.name)
                    .set("license_plate", data.license_plate)
                    .set("status", "ACTIVE")
                    .set("department", "")
                    .set("department_job", "")
                    .set("nucleus", "")
                    .set("nucleus_job", "")
                    .set("office","")
                    .set("time_creation", Timestamp.now())
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.add(user);

            LOG.info("User registered in datastore " + username);
            txn.commit();
            return Response.ok(user).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


}
