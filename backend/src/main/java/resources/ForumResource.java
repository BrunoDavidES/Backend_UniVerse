package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

        String adminID = decodedToken.getUid();
        String adminName = decodedToken.getName();
        String adminRole = getRole(decodedToken);

        String[] roles = {"ADMIN", "TEACHER", "BO", "D"};

        if (!Arrays.asList(roles).contains(adminRole)) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            String date = dateFormat.format(currentDate);

            Map<String, Object> memberData = new HashMap<>();
            memberData.put("name", adminName);
            memberData.put("joined", date);

            DatabaseReference forumsRef = firebaseDatabase.getReference("forums");
            String forumID = forumsRef.push().getKey();
            forumsRef.child(forumID).child("name").setValueAsync(data.getName());
            forumsRef.child(forumID).child("creation").setValueAsync(date);
            forumsRef.child(forumID).child("members").child(adminID).setValueAsync(memberData);

            memberData.replace("name", data.getName());

            firebaseDatabase.getReference("users").child(adminID).child("forums").child(forumID).setValueAsync(memberData);

            Key forumKey = datastore.newKeyFactory().setKind("Forum").newKey(forumID);

            Entity forum = Entity.newBuilder(forumKey)
                    .set("password", data.getPassword())
                    .build();
            txn.add(forum);

            Key userForumKey = datastore.newKeyFactory().setKind("User_Forum")
                    .addAncestor(PathElement.of("Forum", forumID))
                    .newKey(adminID);

            Entity userForum = Entity.newBuilder(userForumKey)
                    .set("role", "ADMIN")
                    .build();
            txn.add(userForum);
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

    @POST
    @Path("/join")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response joinForum(@HeaderParam("Authorization") String token,
                                ForumData data) {

        LOG.fine("Attempt to join forum: " + data.getForumID());

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
            Key forumKey = datastore.newKeyFactory().setKind("Forum").newKey(data.getForumID());

            Entity forum = txn.get(forumKey);
            if(!data.getPassword().equals(forum.getString("password"))) {
                txn.rollback();
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            String date = dateFormat.format(currentDate);

            Map<String, Object> memberData = new HashMap<>();
            memberData.put("name", userName);
            memberData.put("joined", date);

            DatabaseReference forumsRef = firebaseDatabase.getReference("forums");
            String forumID = forumsRef.push().getKey();
            forumsRef.child(forumID).child("members").child(userID).setValueAsync(memberData);

            memberData.replace("name", data.getName());

            firebaseDatabase.getReference("users").child(userID).child("forums").child(forumID).setValueAsync(memberData);

            Key userForumKey = datastore.newKeyFactory().setKind("User_Forum")
                    .addAncestor(PathElement.of("Forum", forumID))
                    .newKey(userID);

            Entity userForum = Entity.newBuilder(userForumKey)
                    .set("role", "ADMIN")
                    .build();
            txn.add(userForum);
            txn.commit();

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

        Transaction txn = datastore.newTransaction();
        try {
            Key userForumKey = datastore.newKeyFactory().setKind("User_Forum")
                    .addAncestor(PathElement.of("Forum", forumID))
                    .newKey(decodedToken.getUid());
            Entity userForum = txn.get(userForumKey);

            if(!userForum.getString("role").equals("ADMIN")) {
                txn.rollback();
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
            }

            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            String date = dateFormat.format(currentDate);

            Map<String, Object> postData = new HashMap<>();
            postData.put("author", decodedToken.getName());
            postData.put("description", data.getDescription());
            postData.put("time", date);

            DatabaseReference forumRef = firebaseDatabase.getReference("forums").child(forumID).child("feed");
            String postID = forumRef.push().getKey();
            forumRef.child(postID).setValueAsync(postData);

            txn.commit();
            LOG.info("Posted to forum");
            return Response.ok(postID).build();
        } catch (Exception e) {
            txn.rollback();
            LOG.info("Error posting to forum");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


}
