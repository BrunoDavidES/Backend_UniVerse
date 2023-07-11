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

/**
 * Resource class for user registration.
 */
@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
    private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    /**
     * Default constructor.
     */
    public RegisterResource() {}

    /**
     * Endpoint for user registration.
     *
     * @param data The user data for registration.
     * @return Response indicating the status of the registration.
     * @throws Exception If an error occurs during registration.
     * It will return 400 error if there are missing or wrong parameters.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(UserData data) throws Exception {
        LOG.fine("Attempt to register user: " + data.getUsername());

        if (!data.validateRegister()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        // Firebase Register
        Response response = firebaseRegister(data);
        if(response.getStatus() == Response.Status.OK.getStatusCode()) {

            // Datastore Register
            response = datastoreRegister(data);
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                firebaseAuth.deleteUser(data.getUsername());
            }
        }

        return response;
    }

    /**
     * Registers the user in Firebase.
     *
     * @param data The user data for registration.
     * @return Response indicating the status of the registration.
     */
    private Response firebaseRegister(UserData data) {
        try {
            UserRecord userRecord = firebaseAuth.createUser( new CreateRequest()
                    .setUid(data.getUsername())
                    .setEmail(data.getEmail())
                    .setEmailVerified(false)
                    .setPassword(data.getPassword())
                    .setDisplayName(data.getName())
                    .setDisabled(false)
            );

            Map<String, Object> customClaims = new HashMap<>();
            customClaims.put(ROLE, data.getRole());
            customClaims.put(LAST_UPDATE, Timestamp.now());
            firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

            LOG.info("User registered in Firebase: " + userRecord.getUid());
            return Response.ok(userRecord).build();
        } catch (FirebaseAuthException e) {
            LOG.warning("Firebase registration failed: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Firebase registration failed: " + e.getMessage()).build();
        }
    }

    /**
     * Registers the user in Datastore.
     *
     * @param data The user data for registration.
     * @return Response indicating the status of the registration.
     */
    private Response datastoreRegister(UserData data) {
        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());

            Entity user = Entity.newBuilder(userKey)
                    .set("email", data.getEmail())
                    .set("name", data.getName())
                    .set("phone", "")
                    .set("license_plate", "")
                    .set("status", "ACTIVE")
                    .set("privacy", "PRIVATE")
                    .set("department", "")
                    .set("department_job", "")
                    .set("nucleus", "")
                    .set("nucleus_job", "")
                    .set("office","")
                    .set("linkedin", "")
                    .set("time_creation", Timestamp.now())
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.add(user);

            LOG.info("User registered in datastore " + data.getUsername());
            txn.commit();
            return Response.ok(user).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


}
