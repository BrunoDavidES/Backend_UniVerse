package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import util.ValToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Logger;

@Path("/list")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    //private static final Datastore datastore = DatastoreOptions.newBuilder().setHost("localhost:8081").setProjectId("id").build().getService();

    public ListResource() { }

    @GET
    @Path("/")
    public Response listUsers(@Context HttpServletRequest request) {
        LOG.fine("Attempt to list users");

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
            String role = token.getClaim("role").asString();
            Transaction txn = datastore.newTransaction();
            try {
                Query<Entity> query;

                if (role.equals("USER")) {
                    query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(StructuredQuery.CompositeFilter.and(
                                    StructuredQuery.PropertyFilter.eq("role", "USER"),
                                    StructuredQuery.PropertyFilter.eq("status", "ACTIVE"),
                                    StructuredQuery.PropertyFilter.eq("privacy", "Public")))
                            .build();
                } else if (role.equals("GBO") || role.equals("GA") || role.equals("GS")) {
                    query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(StructuredQuery.PropertyFilter.eq("role", "USER"))
                            .build();
                } else if (role.equals("SU")) {
                    query = Query.newEntityQueryBuilder().setKind("User").build();
                } else {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Invalid role").build();
                }

                QueryResults<Entity> results1 = datastore.run(query);
                QueryResults<Entity> results2 = null;
                QueryResults<Entity> results3 = null;

                if(role.equals("GA") || role.equals("GS")) {
                    query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(StructuredQuery.PropertyFilter.eq("role", "GBO"))
                            .build();

                    results2 = datastore.run(query);
                }
                if(role.equals("GS")) {
                    query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(StructuredQuery.PropertyFilter.eq("role", "GA"))
                            .build();

                    results3 = datastore.run(query);
                }

                List<String> usernames = new ArrayList<>();
                while (results1.hasNext()) {
                    Entity user = results1.next();
                    String username = user.getKey().getName();
                    usernames.add(username);
                }
                if(results2 != null) {
                    while (results2.hasNext()) {
                        Entity user = results2.next();
                        String username = user.getKey().getName();
                        usernames.add(username);
                    }
                }
                if(results3 != null) {
                    while (results3.hasNext()) {
                        Entity user = results3.next();
                        String username = user.getKey().getName();
                        usernames.add(username);
                    }
                }

                LOG.info("User list created");
                Gson gson = new Gson();
                String json = gson.toJson(usernames);
                txn.commit();
                return Response.ok(json, MediaType.APPLICATION_JSON).build();
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
