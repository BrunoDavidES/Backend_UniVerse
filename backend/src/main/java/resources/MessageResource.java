package resources;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import util.MessageData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/message")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class MessageResource {
    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendMessage(MessageData messageData) {
        try {
            String senderId = messageData.getSenderId();
            String recipientId = messageData.getRecipientId();
            String message = messageData.getMessage();

            DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
            DatabaseReference senderRef = messagesRef.child(senderId).child(recipientId);
            DatabaseReference recipientRef = messagesRef.child(recipientId).child(senderId);

            String messageId = senderRef.push().getKey();
            senderRef.child(messageId).setValueAsync(message);
            recipientRef.child(messageId).setValueAsync(message);

            return Response.ok("Message sent").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
