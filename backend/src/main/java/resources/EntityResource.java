package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import util.MessageData;
import util.ValToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/entity")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EntityResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    //private static final Datastore datastore = DatastoreOptions.newBuilder().setHost("localhost:8081").setProjectId("id").build().getService();

    public EntityResource() { }

    @POST
    @Path("/new/{kind}/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getReceivedInbox(@Context HttpServletRequest request,@PathParam("kind") String kind,@PathParam("key") String keyName, Map<String, String> attributes) {
        LOG.fine("Attempt to create new entity");

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
            //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
        }

            Transaction txn = datastore.newTransaction();
            try {
                Key key = datastore.newKeyFactory().setKind(kind).newKey(keyName);
                Entity.Builder builder = Entity.newBuilder(key);
                for(Map.Entry<String, String> attribute : attributes.entrySet()) {
                    builder.set(attribute.getKey(), attribute.getValue());
                }
                Entity entity = builder.build();
                txn.add(entity);

                LOG.info("Entity Created");
                txn.commit();
                return Response.ok().build();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
    }

}
