package resources;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.*;
import models.MessageData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/message")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class MessageResource {
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendMessage(MessageData messageData) throws FirebaseAuthException {
        if(!messageData.validate()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String chatId = messageData.getChatId();
        boolean single = chatId == null;
        String senderId = messageData.getSenderId();
        List<String> recipientIds = messageData.getRecipientIds();

        if (!userExists(senderId)) {
            return Response.status(Response.Status.NOT_FOUND).entity("Sender does not exist").build();
        }

        for (String recipientId : recipientIds) {
            if (!userExists(recipientId)) {
                return Response.status(Response.Status.NOT_FOUND).entity("Recipient " + recipientId + " does not exist").build();
            }
            if (senderId.equals(recipientId)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Sender and recipient cannot be the same").build();
            }
        }

        try {
            DatabaseReference senderRef = firebaseDatabase.getReference("Users").child(senderId);
            if(single) chatId = recipientIds.get(0);
            String messageId = senderRef.child("inbox").child(chatId).push().getKey();
            senderRef.child("inbox").child(chatId).child(messageId).setValueAsync(messageData);


            if(single) chatId = senderId;
            for(String recipientId : recipientIds) {
                DatabaseReference recipientRef = firebaseDatabase.getReference("Users").child(recipientId);
                recipientRef.child("inbox").child(chatId).child(messageId).setValueAsync(messageData);
            }

            return Response.ok(chatId).build();
        } catch (Exception e) {
            e.printStackTrace();
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
