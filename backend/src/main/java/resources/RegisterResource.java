package resources;

import com.google.cloud.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import at.favre.lib.crypto.bcrypt.BCrypt;
import util.UserData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public RegisterResource() { }

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
            UserRecord user = firebaseAuth.createUser( new CreateRequest()
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

            firebaseAuth.setCustomUserClaims(user.getUid(), customClaims);

            LOG.info("User registered: " + user.getUid());
            return Response.ok(user).build();
        } catch (FirebaseAuthException e) {
            LOG.warning("User registration failed: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("User registration failed: " + e.getMessage()).build();
        }
    }


}
