package resources;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.FirebaseAuth.authenticateToken;

@Path("/{username}")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class BanResource {
    private static final Logger LOG = Logger.getLogger(BanResource.class.getName());

    @POST
    @Path("/ban")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response banUser(@HeaderParam("Authorization") String token,
                            @PathParam("username") String username) {
        LOG.fine("Attempt to ban user.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        /*if(!data.validate()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }*/

        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(username);

            UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(userRecord.getUid())
                    .setDisabled(true);

            FirebaseAuth.getInstance().updateUser(updateRequest);

            LOG.info("User banned");
            return Response.ok(username).build();
        } catch (FirebaseAuthException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to ban user: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/unban")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unbanUser(@HeaderParam("Authorization") String token,
                              @PathParam("username") String username) {
        LOG.fine("Attempt to unban user.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        /*if(!data.validate()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }*/

        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(username);

            UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(userRecord.getUid())
                    .setDisabled(false);

            FirebaseAuth.getInstance().updateUser(updateRequest);

            LOG.info("User unbanned");
            return Response.ok(username).build();
        } catch (FirebaseAuthException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to unban user: " + e.getMessage()).build();
        }
    }

}
