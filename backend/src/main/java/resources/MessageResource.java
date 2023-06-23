package resources;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.*;
import util.MessageData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Path("/message")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class MessageResource {
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendMessage(MessageData messageData) throws FirebaseAuthException {
        String senderId = messageData.getSenderId();
        String recipientId = messageData.getRecipientId();

        if(firebaseAuth.getUser(senderId) == null || firebaseAuth.getUser(recipientId) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            DatabaseReference senderRef = firebaseDatabase.getReference("Users").child(senderId);
            String messageId = senderRef.child("inbox").child(recipientId).push().getKey();
            senderRef.child("inbox").child(recipientId).child(messageId).setValueAsync(messageData);

            DatabaseReference recipientRef = firebaseDatabase.getReference("Users").child(recipientId);
            recipientRef.child("inbox").child(senderId).child(messageId).setValueAsync(messageData);

            return Response.ok("Message sent").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/chat")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChat(@QueryParam("userId") String userId, @QueryParam("targetId") String targetId) {
        try {
            DatabaseReference chatRef = firebaseDatabase.getReference("Users").child(userId).child("inbox").child(targetId);

            CompletableFuture<List<MessageData>> future = new CompletableFuture<>();
            List<MessageData> messages = new ArrayList<>();

            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        MessageData messageData = messageSnapshot.getValue(MessageData.class);
                        messages.add(messageData);
                    }
                    future.complete(messages);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    future.completeExceptionally(databaseError.toException());
                }
            });

            List<MessageData> retrievedMessages = future.get();

            return Response.ok(retrievedMessages).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
