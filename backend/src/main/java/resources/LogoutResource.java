package resources;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;


@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    public LogoutResource() { }

    @POST
    @Path("/")
    public Response logout() {
    // TODO
        return Response.status(Response.Status.BAD_REQUEST).entity("Logout failed").build();
    }


}
