package resources;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.database.*;
import models.MessageData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.FirebaseAuth.*;

@Path("/message")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class MessageResource {
    private static final Logger LOG = Logger.getLogger(MessageResource.class.getName());
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendMessage(@HeaderParam("Authorization") String token,
                                MessageData data) {

        LOG.fine("Attempt to send message to: " + data.getRecipientId());

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if(!data.validate()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        String senderId = data.getSenderId();
        String recipientId = data.getRecipientId();

        if (!userExists(senderId) || !userExists(recipientId)) {
            return Response.status(Response.Status.NOT_FOUND).entity(USER_DOES_NOT_EXIST).build();
        }

        if (senderId.equals(recipientId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Sender and recipient cannot be the same").build();
        }

        senderId = senderId.replace(".", "");
        recipientId = recipientId.replace(".", "");

        try {
            DatabaseReference senderRef = firebaseDatabase.getReference("Users").child(senderId).child("inbox").child(recipientId);
            String messageId = senderRef.push().getKey();
            senderRef.child(messageId).setValueAsync(data);

            DatabaseReference recipientRef = firebaseDatabase.getReference("Users").child(recipientId).child("inbox").child(senderId);
            recipientRef.child(messageId).setValueAsync(data);

            LOG.info("Message sent");
            return Response.ok(messageId).build();
        } catch (Exception e) {
            LOG.info("Error sending message");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean userExists(String userId) {
        try {
            firebaseAuth.getUser(userId);
            return true;
        } catch (FirebaseAuthException e) {
            return false;
        }
    }

}
