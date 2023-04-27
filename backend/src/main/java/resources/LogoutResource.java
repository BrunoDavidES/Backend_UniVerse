package resources;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
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
    public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    cookie.setValue(null);
                    response.addCookie(cookie);
                    LOG.info("Token invalidated");
                    return Response.ok("Logout successful").build();
                }
            }
        }

        LOG.warning("Cookies not found");
        return Response.status(Response.Status.BAD_REQUEST).entity("Logout failed").build();
    }


}
