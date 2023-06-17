package resources;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import at.favre.lib.crypto.bcrypt.BCrypt;
import util.UserData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
    private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());

    public RegisterResource() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("backend/serviceAccountKey.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(UserData data) {
        LOG.fine("Attempt to register user: " + data.username);

        if (!data.validateRegister()) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        try {
            UserRecord userRecord = FirebaseAuth.getInstance().createUser( new CreateRequest()
                    .setUid(data.username)
                    .setEmail(data.email)
                    .setEmailVerified(false)
                    .setPassword(BCrypt.withDefaults().hashToString(12, data.password.toCharArray()))
                    .setDisplayName(data.name)
                    .setPhotoUrl("TODO")
                    .setDisabled(true)
            );

            Map<String, Object> customClaims = new HashMap<>();
            customClaims.put("role", data.getRole());
            customClaims.put("time_creation", Timestamp.now());
            customClaims.put("time_lastupdate", Timestamp.now());

            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), customClaims);

            LOG.info("User registered: " + userRecord.getUid());
            return Response.ok(userRecord).build();
        } catch (FirebaseAuthException e) {
            LOG.warning("User registration failed: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("User registration failed: " + e.getMessage()).build();
        }
    }


}
