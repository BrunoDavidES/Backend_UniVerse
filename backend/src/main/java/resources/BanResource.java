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
import static utils.FirebaseAuth.getRole;


/**
 * Resource class for handling user banning and unbanning.
 */
@Path("/{username}")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class BanResource {
    private static final Logger LOG = Logger.getLogger(BanResource.class.getName());


    /**
     * Bans a user.
     *
     * @param token    The authorization token.
     * @param username The username of the user to ban.
     * @return The response indicating the success or failure of the operation.
     * It will return 401 if the token is doesn't exist or 400 if the
     * user role is not backoffice or admin.
     */
    @POST
    @Path("/ban")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response banUser(@HeaderParam("Authorization") String token,
                            @PathParam("username") String username) {
        LOG.fine("Attempt to ban user.");

        var validation = validateTokenPermissions(token);
        if(validation != null)
            return validation;

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


    /**
     * Unbans a user.
     *
     * @param token    The authorization token.
     * @param username The username of the user to unban.
     * @return The response indicating the success or failure of the operation.
     * It will return 401 if the token is doesn't exist or 400 if the
     * user role is not backoffice or admin.
     */
    @POST
    @Path("/unban")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unbanUser(@HeaderParam("Authorization") String token,
                              @PathParam("username") String username) {
        LOG.fine("Attempt to unban user.");

        var validation = validateTokenPermissions(token);
        if(validation != null)
            return validation;

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


    /**
     * Validates the authorization token.
     *
     * @param token The authorization token.
     * @return The response indicating the success or failure of the validation.
     */
    private Response validateTokenPermissions(String token){
        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        String role = getRole(decodedToken);
        if(!role.equals(BO) && !role.equals(ADMIN)){
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
        }
        return null;
    }

}
