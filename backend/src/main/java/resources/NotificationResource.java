package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.*;
import models.ForumData;
import models.NotificationData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.Constants.TOKEN_NOT_FOUND;
import static utils.FirebaseAuth.authenticateToken;

/**
 * Resource class for handling notifications.
 */
@Path("/notification")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class NotificationResource {
    private static final Logger LOG = Logger.getLogger(NotificationResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    /**
     * Registers a device for receiving notifications.
     *
     * @param token The authorization token.
     * @param data  The notification data.
     * @return The response indicating the success or failure of the registration.
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerDevice(@HeaderParam("Authorization") String token, NotificationData data) {
        LOG.fine("Attempt to register device: " + data.getFcmToken());

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }
        return registerdevice(decodedToken, data);
    }

    /**
     * Sends a notification to the specified targets.
     *
     * @param targets The list of target users.
     */
    public static void sendNotification(List<String> targets, String forum) {
        LOG.warning("Attempt to send notification");
        List<String> targetDevices = new ArrayList<>();

        Transaction txn = datastore.newTransaction();
        try {
            Key key;
            Entity entity;
            Query<Entity> query;
            QueryResults<Entity> results;
            for (String target : targets) {
                key = datastore.newKeyFactory()
                        .setKind("User")
                        .newKey(target);

                query = Query.newEntityQueryBuilder()
                        .setKind("Device")
                        .setFilter(StructuredQuery.PropertyFilter.hasAncestor(key))
                        .build();
                results = txn.run(query);

                while (results.hasNext()) {
                    entity = results.next();
                    LOG.warning(entity.getKey().getName());
                    targetDevices.add(entity.getKey().getName());
                }
            }
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle("UniVerse")
                    .setBody("You have a new message in: " + forum)
                    .build();

            Message message;

            for(String fcmToken: targetDevices) {
                message = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(notification)
                        .build();

                FirebaseMessaging.getInstance().send(message);
            }

        } catch (Exception e) {
            LOG.warning("Failed to send notification" + e.getMessage());
        }

        LOG.warning("Members notified");
    }

    private Response registerdevice(FirebaseToken decodedToken, NotificationData data){
        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory()
                    .setKind("Device")
                    .addAncestors(PathElement.of("User", decodedToken.getUid()))
                    .newKey(data.getFcmToken());

            Entity user = Entity.newBuilder(userKey).build();
            txn.add(user);
            txn.commit();

            List<String> fcmTokens = new ArrayList<>();
            fcmTokens.add(data.getFcmToken());
            FirebaseMessaging.getInstance().subscribeToTopic(fcmTokens, "Alerts");

            LOG.info("Device registered in datastore " + data.getFcmToken());
            return Response.ok(user).build();
        } catch (FirebaseMessagingException e) {
            LOG.info("Subscribe failed");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

}
