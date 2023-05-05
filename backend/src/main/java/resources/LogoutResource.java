package resources;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import util.LogoutData;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private static final Logger Log = Logger.getLogger(LogoutResource.class.getName());

    public LogoutResource() {
    }

    @POST
    @Path("/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response doLogout(LogoutData data) {
        Log.info("User " + data.username + " is trying to logout.");

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.username);

        Transaction txn = datastore.newTransaction();

        try {

            Entity user = txn.get(userKey);

            if (user == null) {
                txn.rollback();
                Log.warning("User " + data.username + " does not exists.");
                return Response.status(Status.BAD_REQUEST).entity("User " + data.username + " does not exists.").build();
            }

            Entity token = txn.get(tokenKey);

            if (token == null) {
                txn.rollback();
                Log.warning("User " + data.username + " is not logged in.");
                return Response.status(Status.FORBIDDEN).entity("User " + data.username + " is not logged in.").build();
            }

            txn.delete(tokenKey);
            Log.info("User " + data.username + " has logged out successfully.");
            txn.commit();
            return Response.ok("{}").entity("User " + data.username + " has logged out successfully.").build();
        }  finally {
            if (txn.isActive()) {
                txn.rollback();
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error 500. Something went wrong with your request.").build();
            }
        }
    }


}
