package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import util.UserData;
import util.ValToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.util.logging.Logger;

@Path("/update")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UpdateResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    //private static final Datastore datastore = DatastoreOptions.newBuilder().setHost("localhost:8081").setProjectId("id").build().getService();

    public UpdateResource() { }

    @PUT
    @Path("/role/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRole(@PathParam("username") String targetname, @Context HttpServletRequest request, UserData data) {
        LOG.fine("Attempt to update user role: " + targetname);

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

            Transaction txn = datastore.newTransaction();
            try {
                Key targetKey = datastore.newKeyFactory().setKind("User").newKey(targetname);
                Entity target = txn.get(targetKey);

                if (target == null) {
                    txn.rollback();
                    LOG.warning("User does not exist");
                    return Response.status(Response.Status.BAD_REQUEST).entity("User does not exist.").build();
                }

                String targetRole = target.getString("role");

                if (userRole.equals("SU")
                        || (userRole.equals("GS") && (targetRole.equals("GA") || targetRole.equals("GBO") || targetRole.equals("USER"))
                        && (data.role.equals("GA") || data.role.equals("GBO") || data.role.equals("USER")))
                        || (userRole.equals("GA") && (targetRole.equals("GBO") || targetRole.equals("USER"))
                        && (data.role.equals("GBO") || data.role.equals("USER"))))
                {
                    target = Entity.newBuilder(target)
                            .set("role", data.role)
                            .set("time_lastupdate", Timestamp.now())
                            .build();
                    txn.put(target);

                    LOG.info("User Role Updated " + targetname);
                    txn.commit();
                    return Response.ok(target).build();
                } else {
                    txn.rollback();
                    LOG.warning("User does not have permission");
                    return Response.status(Response.Status.BAD_REQUEST).entity("User does not have permission.").build();
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

    @PUT
    @Path("/status/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateStatus(@PathParam("username") String targetname, @Context HttpServletRequest request, UserData data) {
        LOG.fine("Attempt to update user state: " + targetname);

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
                    target = Entity.newBuilder(target)
                            .set("status", data.status)
                            .set("time_lastupdate", Timestamp.now())
                            .build();
                    txn.put(target);

                    LOG.info("User Status Updated " + targetname);
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

    @PUT
    @Path("/attributes/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAttributes(@PathParam("username") String targetname, @Context HttpServletRequest request, UserData data) {
        LOG.fine("Attempt to update user attributes: " + targetname);

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
                    target = Entity.newBuilder(target)
                            .set("landline", data.landline)
                            .set("mobile", data.mobile)
                            .set("address", data.address)
                            .set("complementary", data.complementary)
                            .set("city", data.city)
                            .set("postcode", data.postcode)
                            .set("workplace", data.workplace)
                            .set("occupation", data.occupation)
                            .set("nif", data.nif)
                            .set("privacy", data.privacy)
                            .set("time_lastupdate", Timestamp.now())
                            .build();
                    txn.put(target);

                    LOG.info("User attributes Updated " + targetname);
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

    @PUT
    @Path("/credentials/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCredentials(@PathParam("username") String targetname, @Context HttpServletRequest request, UserData data) {
        LOG.fine("Attempt to update user credentials: " + targetname);

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
                        || (userRole.equals("GBO") && targetRole.equals("USER")))
                {
                    target = Entity.newBuilder(target)
                            .set("name", data.name)
                            .set("email", data.email)
                            .set("time_lastupdate", Timestamp.now())
                            .build();
                    txn.put(target);

                    LOG.info("User credentials Updated " + targetname);
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

    @PUT
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassword(@Context HttpServletRequest request, UserData data) {
        LOG.fine("Attempt to update user password");

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
            String username = token.getClaim("user").asString();

            Transaction txn = datastore.newTransaction();
            try {
                Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
                Entity user = txn.get(userKey);

                if (user == null) {
                    txn.rollback();
                    LOG.warning("User does not exist");
                    return Response.status(Response.Status.BAD_REQUEST).entity("User does not exist").build();
                }

                String password = user.getString("password");
                String confirmation = DigestUtils.sha512Hex(data.confirmation);

                if (password.equals(confirmation)) {
                    user = Entity.newBuilder(user)
                            .set("password", DigestUtils.sha512Hex(data.password))
                            .set("time_lastupdate", Timestamp.now())
                            .build();
                    txn.put(user);

                    LOG.info("User password Updated " + username);
                    txn.commit();
                    return Response.ok(user).build();
                } else {
                    txn.rollback();
                    LOG.warning("Password confirmation failed");
                    return Response.status(Response.Status.BAD_REQUEST).entity("Password confirmation failed").build();
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
