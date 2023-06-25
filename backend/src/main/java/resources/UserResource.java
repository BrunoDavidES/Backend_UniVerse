package resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

// Operations related to a single user, such as grades, enrolled subjects, calendar...
@Path("/subject")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UserResource {
    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/todo")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response todo() {
        // TODO
        return Response.ok().build();
    }
}
