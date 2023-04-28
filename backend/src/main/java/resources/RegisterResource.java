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
                Entity.Builder builder = Entity.newBuilder(userKey);
                if(true){
                    builder.set("email", data.email);
                }
                if(true){
                    builder.set("name", data.name);
                }
                if(true){
                    builder.set("password", DigestUtils.sha512Hex(data.password));
                }

                if(true){
                    builder.set("landline", data.landline);
                }else {
                    builder.set("landline", "EMPTY");
                }

                if(true){
                    builder.set("mobile", data.mobile);
                }else {
                    builder.set("mobile", "EMPTY");
                }

                if(true){
                    builder.set("address", data.address);
                }else {
                    builder.set("address", "EMPTY");
                }

                if(true){
                    builder.set("complementary", data.complementary);
                }else {
                    builder.set("complementary", "EMPTY");
                }

                if(true){
                    builder.set("postcode", data.postcode);
                }else {
                    builder.set("postcode", "EMPTY");
                }

                if(true){
                    builder .set("workplace", data.workplace);
                }else {
                    builder.set("workplace", "EMPTY");
                }

                if(true){
                    builder.set("occupation", data.occupation);
                }else {
                    builder.set("occupation", "EMPTY");
                }

                if(true){
                    builder.set("nif", data.nif);
                }else {
                    builder.set("nif", "EMPTY");
                }

                if(true){
                    builder.set("email", data.email);
                }else {
                    builder.set("email", "EMPTY");
                }

                if(true){
                    builder.set("privacy", data.privacy);
                }else {
                    builder.set("privacy", "EMPTY");
                }

                if(true){
                    builder.set("laranja", "toranja");
                }

                if(true){
                    builder.set("villager", "gimme emeralds");
                }

                if(true){
                    builder.set("role", role);
                }else {
                    builder.set("role", "EMPTY");
                }

                if(true){
                    builder.set("status", status);
                }else {
                    builder.set("status", "EMPTY");
                }
                    builder.set("time_creation", Timestamp.now());
                    builder.set("time_lastupdate", Timestamp.now());
                Entity entity = builder.build();
                txn.add(entity);

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
