package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import util.ValToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.util.logging.Logger;

@Path("/token")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class TokenResources {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    public TokenResources() { }

    @GET
    @Path("/")
    public Response getToken(@Context HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    LOG.info("Token found");
                    return Response.ok(cookie.getValue()).build();
                }
            }
        } else {
            LOG.warning("No cookies found");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No cookies found").build();
        }
        LOG.warning("Token not found");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
    }

    @GET
    @Path("/validate")
    public Response validateToken(@Context HttpServletRequest request) {
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
            LOG.warning("Token not found in validateToken");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
        }

        try {
            DecodedJWT token = validator.validateToken(codedToken);

            String iss = token.getClaim("iss").asString();
            String jti = token.getClaim("jti").asString();
            int exp = token.getClaim("exp").asInt();
            int iat = token.getClaim("iat").asInt();
            String role = token.getClaim("role").asString();
            String status = token.getClaim("status").asString();
            String user = token.getClaim("user").asString();

            LOG.info("Token validated for user " + user);
            return Response.ok(iss + "<br>" + jti + "<br>" + exp + "<br>" + iat + "<br>" + role + "<br>" + status + "<br>" + user).build();
        } catch (InvalidParameterException e) {
            LOG.warning("Invalid token in validateToken: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Token is invalid").build();
        }
    }

}
