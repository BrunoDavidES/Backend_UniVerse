package resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import util.UserData;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    //private static final Datastore datastore = DatastoreOptions.newBuilder().setHost("localhost:8081").setProjectId("id").build().getService();

    public RegisterResource() { }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(UserData data) {
        LOG.fine("Attempt to register user: " + data.username);

        if( !data.validateRegister() ) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);

            if( user != null ) {
                txn.rollback();
                LOG.warning("User already exists");
                return Response.status(Response.Status.BAD_REQUEST).entity("User already exists").build();
            } else {
                Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();
                QueryResults<Entity> results = datastore.run(query);
                String role;
                String status;
                if (!results.hasNext()) {
                    role = "SU";
                    status = "ACTIVE";
                } else {
                    role = "USER";
                    status = "INACTIVE";
                }
                user = Entity.newBuilder(userKey)
                        .set("email", data.email)
                        .set("name", data.name)
                        .set("password", DigestUtils.sha512Hex(data.password))
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
                        .set("role", role)
                        .set("status", status)
                        .set("time_creation", Timestamp.now())
                        .set("time_lastupdate", Timestamp.now())
                        .build();
                txn.add(user);

                LOG.info("User registered " + data.username);
                txn.commit();
                return Response.ok(user).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

}
