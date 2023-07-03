package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import models.ForumData;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import static utils.Constants.*;
import static utils.FirebaseAuth.*;

@Path("/forum")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ForumResource {
    private static final Logger LOG = Logger.getLogger(ForumResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public ForumResource() {}

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createForum(@HeaderParam("Authorization") String token,
                                ForumData data) {

        LOG.fine("Attempt to create forum: " + data.getName());

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        if(!data.validate()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        String userID = decodedToken.getUid();
        String userName = decodedToken.getName();
        String userRole = getRole(decodedToken);

        /*String[] roles = {"ADMIN", "TEACHER", "BO", "D"};

        if (!Arrays.asList(roles).contains(userRole)) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }*/

        Transaction txn = datastore.newTransaction();
        try {
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            String date = dateFormat.format(currentDate);

            Map<String, Object> memberData = new HashMap<>();
            memberData.put("name", userName);
            memberData.put("role", "ADMIN");
            memberData.put("joined", date);

            DatabaseReference forumsRef = firebaseDatabase.getReference("forums");
            String forumID = forumsRef.push().getKey();
            forumsRef.child(forumID).child("name").setValueAsync(data.getName());
            forumsRef.child(forumID).child("description").setValueAsync(data.getDescription());
            forumsRef.child(forumID).child("creation").setValueAsync(date);
            forumsRef.child(forumID).child("updated").setValueAsync(date);
            forumsRef.child(forumID).child("members").child(userID.replace(".", "-")).setValueAsync(memberData);

            memberData.replace("name", data.getName());

            firebaseDatabase.getReference("users")
                    .child(userID.replace(".", "-"))
                    .child("forums")
                    .child(forumID)
                    .setValueAsync(memberData);

            Key forumKey = datastore.newKeyFactory().setKind("Forum").newKey(forumID);

            Entity forum = Entity.newBuilder(forumKey)
                    .set("password", data.getPassword())
                    .build();
            txn.add(forum);
            txn.commit();

            LOG.info("Forum created");
            return Response.ok(forumID).build();
        } catch (Exception e) {
            LOG.info("Error creating forum");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/{forumID}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createForum(@HeaderParam("Authorization") String token,
                                @PathParam("forumID") String forumID) {

        LOG.fine("Attempt to delete forum: " + forumID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        String userID = decodedToken.getUid();
        String forumRole = getForumRole(forumID, userID);

        /*
        if (!forumRole.equals(ADMIN)) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }*/

        Transaction txn = datastore.newTransaction();
        try {
            firebaseDatabase.getReference("forums").child(forumID).removeValueAsync();

            List<String> membersIDs = getForumMembers(forumID);

            for(String memberID: membersIDs) {
                firebaseDatabase.getReference("users")
                        .child(memberID)
                        .child("forums")
                        .child(forumID)
                        .removeValueAsync();
            }

            Key forumKey = datastore.newKeyFactory().setKind("Forum").newKey(forumID);
            txn.delete(forumKey);
            txn.commit();

            LOG.info("Forum deleted");
            return Response.ok(forumID).build();
        } catch (Exception e) {
            LOG.info("Error deleting forum");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/{forumID}/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateForum(@HeaderParam("Authorization") String token,
                                @PathParam("forumID") String forumID,
                                ForumData data) {

        LOG.fine("Attempt to update forum: " + forumID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        String userID = decodedToken.getUid();
        String forumRole = getForumRole(forumID, userID);

        /*
        if (!forumRole.equals(ADMIN)) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }*/

        try {
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            String date = dateFormat.format(currentDate);

            DatabaseReference forumRef = firebaseDatabase.getReference("forums").child(forumID);
            forumRef.child("description").setValueAsync(data.getDescription());
            forumRef.child("updated").setValueAsync(date);

            LOG.info("Forum updated");
            return Response.ok(forumID).build();
        } catch (Exception e) {
            LOG.info("Error updating forum");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{forumID}/post")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendMessage(@HeaderParam("Authorization") String token,
                                @PathParam("forumID") String forumID,
                                ForumData data) {

        LOG.fine("Attempt to post to forum: " + data.getName());

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        String userID = decodedToken.getUid();
        String forumRole = getForumRole(forumID, userID);

        /*
        if (!forumRole.equals(ADMIN) || !forumRole.equals(ASSISTANT) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }*/

        try {
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            String date = dateFormat.format(currentDate);

            Map<String, Object> postData = new HashMap<>();
            postData.put("author", decodedToken.getName());
            postData.put("message", data.getPost());
            postData.put("creation", date);

            DatabaseReference forumRef = firebaseDatabase.getReference("forums").child(forumID).child("feed");
            String postID = forumRef.push().getKey();
            forumRef.child(postID).setValueAsync(postData);

            LOG.info("Posted to forum");
            return Response.ok(postID).build();
        } catch (Exception e) {
            LOG.info("Error posting to forum");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{forumID}/{postID}/edit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editPost(@HeaderParam("Authorization") String token,
                             @PathParam("forumID") String forumID,
                             @PathParam("postID") String postID,
                             ForumData data) {

        LOG.fine("Attempt to edit post: " + postID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        String userID = decodedToken.getUid();
        String author = getPostAuthor(forumID, postID);

        if(!userID.equals(author)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        try {
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            String date = dateFormat.format(currentDate);

            DatabaseReference postRef = firebaseDatabase.getReference("forums")
                    .child(forumID)
                    .child("feed")
                    .child(postID);
            postRef.child("message").setValueAsync(data.getPost());
            postRef.child("edited").setValueAsync(date);

            LOG.info("Edited post");
            return Response.ok(author).build();
        } catch (Exception e) {
            LOG.info("Error editing post");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{forumID}/{postID}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removePost(@HeaderParam("Authorization") String token,
                             @PathParam("forumID") String forumID,
                             @PathParam("postID") String postID) {

        LOG.fine("Attempt to remove post: " + postID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        String userID = decodedToken.getUid();
        String forumRole = getForumRole(forumID, userID);
        String author = getPostAuthor(forumID, postID);

        if(!userID.equals(author) || !forumRole.equals(ADMIN)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        try {
            firebaseDatabase.getReference("forums")
                    .child(forumID)
                    .child("feed")
                    .child(postID)
                    .removeValueAsync();

            LOG.info("Removed post");
            return Response.ok(author).build();
        } catch (Exception e) {
            LOG.info("Error removing post");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{forumID}/{memberID}/promote")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response promoteMember(@HeaderParam("Authorization") String token,
                                @PathParam("forumID") String forumID,
                                @PathParam("memberID") String memberID) {

        LOG.fine("Attempt to promote user: " + memberID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        String userID = decodedToken.getUid();
        String userRole = getForumRole(forumID, userID);
        String memberRole = getForumRole(forumID, memberID);

        if (!userRole.equals(ADMIN)) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        String promotedRole;
        if(memberRole.equals("MEMBER")) {
            promotedRole = "ASSISTANT";
        } else if(memberRole.equals("ASSISTANT")) {
            promotedRole = "ADMIN";
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        try {
            firebaseDatabase.getReference("forums")
                    .child(forumID)
                    .child("members")
                    .child(memberID.replace(".", "-"))
                    .child("role")
                    .setValueAsync(promotedRole);

            firebaseDatabase.getReference("users")
                    .child(userID.replace(".", "-"))
                    .child("forums")
                    .child(forumID)
                    .child("role")
                    .setValueAsync(promotedRole);

            LOG.info("Member promoted");
            return Response.ok(userRole).build();
        } catch (Exception e) {
            LOG.info("Error promoting member");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{forumID}/{memberID}/demote")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response demoteMember(@HeaderParam("Authorization") String token,
                                @PathParam("forumID") String forumID,
                                @PathParam("memberID") String memberID) {

        LOG.fine("Attempt to demote user: " + memberID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        String userID = decodedToken.getUid();
        String userRole = getForumRole(forumID, userID);
        String memberRole = getForumRole(forumID, memberID);

        if (!userRole.equals(ADMIN)) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        String demotedRole;
        if(memberRole.equals("ASSISTANT")) {
            demotedRole = "MEMBER";
        } else if(memberRole.equals("ADMIN")) {
            demotedRole = "ASSISTANT";
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        try {
            firebaseDatabase.getReference("forums")
                    .child(forumID)
                    .child("members")
                    .child(memberID.replace(".", "-"))
                    .child("role")
                    .setValueAsync(demotedRole);

            firebaseDatabase.getReference("users")
                    .child(userID.replace(".", "-"))
                    .child("forums")
                    .child(forumID)
                    .child("role")
                    .setValueAsync(demotedRole);

            LOG.info("Member demoted");
            return Response.ok(userRole).build();
        } catch (Exception e) {
            LOG.info("Error demoting member");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{forumID}/join")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response joinForum(@HeaderParam("Authorization") String token,
                              @PathParam("forumID") String forumID,
                              ForumData data) {

        LOG.fine("Attempt to join forum: " + forumID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        /*if(!data.validate()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }*/

        String userID = decodedToken.getUid();
        String userName = decodedToken.getName();

        Transaction txn = datastore.newTransaction();
        try {
            Key forumKey = datastore.newKeyFactory().setKind("Forum").newKey(forumID);

            Entity forum = txn.get(forumKey);
            if(!data.getPassword().equals(forum.getString("password"))) {
                txn.rollback();
                return Response.status(Response.Status.FORBIDDEN).entity("Incorrect Credentials").build();
            }

            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            String date = dateFormat.format(currentDate);

            // NEED TO CHANGE ONLY FOR TESTING
            Map<String, Object> memberData = new HashMap<>();
            memberData.put("name", userName);
            memberData.put("role", "ADMIN");
            memberData.put("joined", date);

            firebaseDatabase.getReference("forums")
                    .child(forumID)
                    .child("members")
                    .child(userID.replace(".", "-"))
                    .setValueAsync(memberData);

            memberData.replace("name", data.getName());

            firebaseDatabase.getReference("users")
                    .child(userID.replace(".", "-"))
                    .child("forums")
                    .child(forumID)
                    .setValueAsync(memberData);

            LOG.info("Member joined forum");
            return Response.ok(forumID).build();
        } catch (Exception e) {
            txn.rollback();
            LOG.info("Error joining forum");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/{forumID}/leave")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response joinForum(@HeaderParam("Authorization") String token,
                              @PathParam("forumID") String forumID) {

        LOG.fine("Attempt to leave forum: " + forumID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        /*if(!data.validate()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }*/

        String userID = decodedToken.getUid();

        try {
            firebaseDatabase.getReference("forums")
                    .child(forumID).child("members")
                    .child(userID.replace(".", "-"))
                    .removeValueAsync();

            firebaseDatabase.getReference("users")
                    .child(userID.replace(".", "-"))
                    .child("forums")
                    .child(forumID)
                    .removeValueAsync();

            LOG.info("Member left forum");
            return Response.ok(forumID).build();
        } catch (Exception e) {
            LOG.info("Error leaving forum");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getForumRole(String forumID, String userID) {
        final String[] role = new String[1];

        firebaseDatabase.getReference("forums")
                .child(forumID)
                .child("members")
                .child(userID.replace(".", "-"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            role[0] = dataSnapshot.child("role").getValue(String.class);
                        } else {
                            role[0] = null;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        role[0] = null;
                    }
                });
        return role[0];
    }

    private List<String> getForumMembers(String forumID) {
        final List<String> members = new ArrayList<>();

        firebaseDatabase.getReference("forums")
                .child(forumID)
                .child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                                String memberID = memberSnapshot.getKey();
                                members.add(memberID);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        return members;
    }

    private String getPostAuthor(String forumID, String postID) {
        final String[] author = new String[1];

        firebaseDatabase.getReference("forums")
                .child(forumID)
                .child("feed")
                .child(postID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            author[0] = dataSnapshot.child("author").getValue(String.class);
                        } else {
                            author[0] = null;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        author[0] = null;
                    }
                });
        return author[0];
    }


}
