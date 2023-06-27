package resources;

import com.google.appengine.api.mail.*;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.database.FirebaseDatabase;
import util.UserData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static util.Constants.*;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
    private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public RegisterResource() {}

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(UserData data) throws FirebaseAuthException {
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
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                emailVerification(data.email);
            }
            else {
                firebaseAuth.deleteUser(data.username);
            }
        }

        return response;
    }

    public void emailVerification(String email) throws FirebaseAuthException {
        String link = firebaseAuth.generateEmailVerificationLink(email);

        try {
            MailService.Message message = new MailService.Message();
            message.setSender("from@example.com");
            message.setTo(email);
            message.setSubject("Example email");
            message.setTextBody("Click the following link to verify your email:\n\n" + link);

            MailService mailService = MailServiceFactory.getMailService();
            mailService.send(message);

            LOG.info("Verification email sent to: " + email);
        } catch (Exception e) {
            LOG.warning("Failed to send verification email to: " + email);
        }
    }

    private Response firebaseRegister(String username, String email, String password, String name, String role) {
        try {
            UserRecord userRecord = firebaseAuth.createUser( new CreateRequest()
                    .setUid(username)
                    .setEmail(email)
                    .setEmailVerified(false)
                    .setPassword(password)
                    .setDisplayName(name)
                    .setDisabled(true)
            );

            Map<String, Object> customClaims = new HashMap<>();
            customClaims.put("role", role);
            firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

            LOG.info("User registered in Firebase: " + userRecord.getUid());
            return Response.ok(userRecord).build();
        } catch (FirebaseAuthException e) {
            LOG.warning("Firebase registration failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Firebase registration failed: " + e.getMessage()).build();
        }
    }

    private Response datastoreRegister(String username, String email, String name, String license_plate) {
        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);

            if(license_plate == null)
                license_plate = UNREGISTERED;

            Entity user = Entity.newBuilder(userKey)
                    .set("email", email)
                    .set("name", name)
                    .set("license_plate", license_plate)
                    .set("job_list", "")
                    .set("personal_event_list", "")  //#string%string%string%string#string%...
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
