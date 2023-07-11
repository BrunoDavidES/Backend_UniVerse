package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.FirebaseAuth.authenticateToken;
import static utils.FirebaseAuth.getRole;

/**
 * Default constructor for the EntityResource class.
 */
@Path("/entity")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EntityResource {
    private static final Logger LOG = Logger.getLogger(EntityResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public EntityResource() { }

    /**
     * Creates a new entity.
     *
     * @param token      the authorization token
     * @param kind       the kind of the entity
     * @param keyName    the name of the key for the entity
     * @param attributes the attributes of the entity
     * @return the response indicating the success or failure of the operation
     */
    @POST
    @Path("/new/{kind}/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getReceivedInbox(@HeaderParam("Authorization") String token, @PathParam("kind") String kind,@PathParam("key") String keyName, Map<String, String> attributes) {
        LOG.fine("Attempt to create new entity");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            /*
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.getClaim("user").toString());
            Entity user = txn.get(userKey);
            */
            if(!getRole(decodedToken).equals(BO)){
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }
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
