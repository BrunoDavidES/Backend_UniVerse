package resources;

import com.google.cloud.datastore.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.logging.Logger;

@Path("/entity")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EntityResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public EntityResource() { }

    @POST
    @Path("/new/{kind}/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getReceivedInbox(@Context HttpServletRequest request,@PathParam("kind") String kind,@PathParam("key") String keyName, Map<String, String> attributes) {
        LOG.fine("Attempt to create new entity");

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
