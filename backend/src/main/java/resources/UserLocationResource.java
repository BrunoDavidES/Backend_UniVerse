package resources;

import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import models.UserLocationData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import static utils.FirebaseAuth.authenticateToken;

@Path("/user/location")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UserLocationResource {
    private static final Logger LOG = Logger.getLogger(ForumResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public UserLocationResource() {}

    @PUT
    @Path("/permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePermissions(@HeaderParam("Authorization") String token,
                                      UserLocationData data) {

        LOG.fine("Attempt to change user location permissions");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            DatabaseReference locationRef = firebaseDatabase.getReference("users")
                    .child(decodedToken.getUid())
                    .child("location");
            locationRef.child("allowed").setValueAsync(data.getAllowed());

            Key userPrivacyKey = datastore.newKeyFactory().setKind("User_Privacy").newKey(decodedToken.getUid());
            Entity userPrivacy = txn.get(userPrivacyKey);

            userPrivacy = Entity.newBuilder(userPrivacy)
                    .set("doNotDisturb", data.isDoNotDisturb())
                    .build();
            txn.put(userPrivacy);
            txn.commit();

            LOG.info("User location permissions updated");
            return Response.ok().build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLocation(@HeaderParam("Authorization") String token,
                                   UserLocationData data) {

        LOG.fine("Attempt to update user location");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        try {
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String date = dateFormat.format(currentDate);

            DatabaseReference locationRef = firebaseDatabase.getReference("users")
                    .child(decodedToken.getUid())
                    .child("location");
            locationRef.child("coordinates").setValueAsync(data.getLocation());
            locationRef.child("updated").setValueAsync(date);

            LOG.info("User location updated");
            return Response.ok(data.getLocation()).build();
        } catch (Exception e) {
            LOG.info("Error updating user location");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{username}/request")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getLocation(@HeaderParam("Authorization") String token,
                                @PathParam("username") String username) {

        LOG.fine("Attempt to request user location: " + username);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userPrivacyKey = datastore.newKeyFactory().setKind("User_Privacy").newKey(username);
            Entity userPrivacy = txn.get(userPrivacyKey);
            boolean doNotDisturb = Boolean.parseBoolean(userPrivacy.getString("doNotDisturb"));

            if(doNotDisturb) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("User is not accepting requests").build();
            }

            // TODO

            txn.commit();

            LOG.info("User location request sent");
            return Response.ok(doNotDisturb).build();
        } catch (Exception e) {
            txn.rollback();
            LOG.info("Error sending location request");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


}
