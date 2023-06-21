package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import util.AuthToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {
    private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());

    @POST
    @Path("/")
    public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        LOG.fine("Attempt to logout user.");

        AuthToken.invalidateToken(request, response);
        return Response.ok().build();
    }




}
