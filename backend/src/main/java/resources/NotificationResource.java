package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.*;
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


@Path("/notification")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class NotificationResource {
    private static final Logger LOG = Logger.getLogger(NotificationResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

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

    public static void sendNotification(List<String> targets) {
        List<String> targetDevices = new ArrayList<>();

        Transaction txn = datastore.newTransaction();
        try {
            Key key;
            Entity entity;
            Query<Entity> query;
            QueryResults<Entity> results;
            for(String target: targets) {
                key = datastore.newKeyFactory()
                        .setKind("Device")
                        .addAncestors(PathElement.of("User", target))
                        .newKey(target);

                 query = Query.newEntityQueryBuilder()
                        .setKind("Device")
                        .setFilter(StructuredQuery.PropertyFilter.hasAncestor(key))
                        .build();
                 results = txn.run(query);

                while (results.hasNext()) {
                    entity = results.next();
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
                    .setBody("This is a test notification")
                    .build();

            Message message;

            for(String fcmToken: targetDevices) {
                message = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(notification)
                        .build();

                FirebaseMessaging.getInstance().send(message);
            }

        } catch (Exception ignored) {
        }

    }


}
