package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import util.NucleusData;
import util.ValToken;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/nucleus")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class NucleusResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public NucleusResource() { }


    /*
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@Context HttpServletRequest request, NucleusData data) {
        LOG.fine("Attempt to create a nucleus by: " + data.creatorEmail);

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

            Key userKey = datastore.newKeyFactory().setKind("Nucleus").newKey(data.nucleusEmail);
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
                        .set("role", data.getRole())
                        .set("status", "ACTIVE")
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
    */

}
