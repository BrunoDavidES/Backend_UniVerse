package resources;

import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.logging.Logger;

import static util.FirebaseAuth.*;
import static util.Constants.*;

@Path("/entity")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EntityResource {
    private static final Logger LOG = Logger.getLogger(EntityResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public EntityResource() { }

    @POST
    @Path("/new/{kind}/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getReceivedInbox(@HeaderParam("Authorization") String token,
                                     @PathParam("kind") String kind,@PathParam("key") String keyName,
                                     Map<String, String> attributes) {
        LOG.fine("Attempt to create new entity");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if(!getRole(decodedToken).equals(BO)){
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
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
                txn.commit();

                LOG.info("Entity Created");
                return Response.ok().build();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
    }

}
