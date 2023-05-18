package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import util.UserData;
import util.ValToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public RegisterResource() { }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@Context HttpServletRequest request, UserData data) {
        LOG.fine("Attempt to register user: " + data.username);

        if( !data.validateRegister() ) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        Transaction txn = datastore.newTransaction();
        try {

            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
            }

            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);

            if( user != null ) {
                txn.rollback();
                LOG.warning("User already exists");
                return Response.status(Response.Status.BAD_REQUEST).entity("User already exists").build();
            } else {

                user = Entity.newBuilder(userKey)
                        .set("email", data.email)
                        .set("name", data.name)
                        .set("password", DigestUtils.sha512Hex(data.password))
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

    /*@POST
    @Path("/new/{kind}/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getReceivedInbox(@Context HttpServletRequest request, @PathParam("kind") String kind, @PathParam("key") String keyName, Map<String, String> attributes) {
        LOG.fine("Attempt to create new entity");



        Transaction txn = datastore.newTransaction();
        Key key = datastore.newKeyFactory().setKind("Role").newKey(keyName);

        try {
            Entity role = txn.get(key);
            if(role == null){
                txn.rollback();
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            Entity.Builder builder = Entity.newBuilder(key);
            for(Map.Entry<String, String> attribute : attributes.entrySet()) {
                builder.set(attribute.getKey(), attribute.getValue());
            }
            Entity entity = builder.build();
            txn.put(entity);

            LOG.info("Role Created");
            txn.commit();
            return Response.ok().build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }*/

    /*@POST
    @Path("/v2")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerV2(UserData data) {
        String[] emails = data.getEmails();
        for (String email : emails) {
            String username = email.split("@")[0];
            LOG.fine("Attempt to register user: " + username);

            if (!data.validateRegister()) {
                LOG.warning("Missing or wrong parameter");
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
            }

            Transaction txn = datastore.newTransaction();
            try {
                Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
                Entity user = txn.get(userKey);

                if (user != null) {
                    txn.rollback();
                    LOG.warning("User already exists");
                    //return Response.status(Response.Status.BAD_REQUEST).entity("User already exists").build();
                } else {
                    Entity.Builder builder = Entity.newBuilder(userKey);

                    String password = UUID.randomUUID().toString();

                    builder.set("name", data.name)
                            .set("password", DigestUtils.sha512Hex(password))
                            .set("email", data.email)
                            .set("role", data.role);

                    for (String[] attribute : data.attributes) {
                        builder.set(attribute[0], attribute[1]);
                    }

                    user = builder.build();
                    txn.add(user);

                    LOG.info("User registered " + data.username);
                    txn.commit();
                }
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        }
        return Response.ok().build();
    }*/


}
