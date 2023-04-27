package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.*;
import util.ValToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.util.logging.Logger;

@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    //private static final Datastore datastore = DatastoreOptions.newBuilder().setHost("localhost:8081").setProjectId("id").build().getService();

    public RemoveResource() { }

    @DELETE
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(@PathParam("username") String targetname, @Context HttpServletRequest request) {
        LOG.fine("Attempt to remove user account: " + targetname);

        final ValToken validator = new ValToken();
        String codedToken = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    codedToken = cookie.getValue();
                    break;
                }
            }
        }

        if (codedToken == null) {
            LOG.warning("Token not found");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
        }

        try {
            DecodedJWT token = validator.validateToken(codedToken);
            String userRole = token.getClaim("role").asString();
            String username = token.getClaim("user").asString();

            Transaction txn = datastore.newTransaction();
            try {
                Key targetKey = datastore.newKeyFactory().setKind("User").newKey(targetname);
                Entity target = txn.get(targetKey);

                if (target == null) {
                    txn.rollback();
                    LOG.warning("User does not exist");
                    return Response.status(Response.Status.BAD_REQUEST).entity("User does not exist").build();
                }

                String targetRole = target.getString("role");

                if (userRole.equals("SU")
                        || (userRole.equals("GS") && (targetRole.equals("GA") || targetRole.equals("GBO") || targetRole.equals("USER")))
                        || (userRole.equals("GA") && (targetRole.equals("GBO") || targetRole.equals("USER")))
                        || (userRole.equals("GBO") && targetRole.equals("USER"))
                        || (username.equals(targetname)))
                {
                    txn.delete(targetKey);

                    LOG.info("User deleted " + targetname);
                    txn.commit();
                    return Response.ok(target).build();
                } else {
                    txn.rollback();
                    LOG.warning("User does not have permission");
                    return Response.status(Response.Status.BAD_REQUEST).entity("User does not have permission").build();
                }
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        } catch (InvalidParameterException e) {
            LOG.warning("Token is invalid: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Token is invalid").build();
        }
    }


}
