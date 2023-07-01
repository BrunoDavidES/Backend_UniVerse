package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import models.ForumData;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
    public Response sendMessage(@HeaderParam("Authorization") String token,
                                ForumData data) {

        LOG.fine("Attempt to create forum: " + data.getName());

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if(!data.validate()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        String adminID = decodedToken.getUid();
        String adminName = decodedToken.getName();
        String adminRole = getRole(decodedToken);
        String kind = "FORUM";

        if(adminRole.equals("NUCLEUS_PRESIDENT")) {
            kind = "NUCLEUS";
        }

        Transaction txn = datastore.newTransaction();
        try {
            Map<String, Object> memberData = new HashMap<>();
            memberData.put("name", adminName);
            memberData.put("role", "ADMIN");

            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String date = dateFormat.format(currentDate);

            DatabaseReference forumsRef = firebaseDatabase.getReference("forums");
            String forumID = forumsRef.push().getKey();
            forumsRef.child(forumID).child("creation").setValueAsync(date);

            Key forumKey = datastore.newKeyFactory().setKind("Forum").newKey(forumID);

            Entity forum = Entity.newBuilder(forumKey)
                    .set("kind", kind)
                    .set("name", data.getName())
                    .set("creation", date)
                    .set("password", data.getPassword())
                    .build();
            txn.add(forum);

            Key userForumKey = datastore.newKeyFactory().setKind("User_Forum")
                    .addAncestor(PathElement.of("Forum", forumID))
                    .newKey(adminID);

            Entity userForum = Entity.newBuilder(userForumKey)
                    .set("name", data.getName())
                    .set("role", "ADMIN")
                    .set("joined", date)
                    .build();
            txn.add(userForum);
            txn.commit();

            LOG.info("Forum created");
            return Response.ok(forumID).build();
        } catch (Exception e) {
            txn.rollback();
            LOG.info("Error creating forum");
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

        Key forumKey = datastore.newKeyFactory().setKind("Forum").newKey(forumID);
        Entity forum = datastore.get(forumKey);

        if(forum == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Forum doesn't exist").build();
        }

        Key userForumKey = datastore.newKeyFactory().setKind("User_Forum")
                .addAncestor(PathElement.of("Forum", forumID))
                .newKey(decodedToken.getUid());
        Entity userForum = datastore.get(userForumKey);

        if(!userForum.getString("role").equals("ADMIN")) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        try {
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
            String date = dateFormat.format(currentDate);

            Map<String, Object> postData = new HashMap<>();
            postData.put("title", data.getTitle());
            postData.put("description", data.getDescription());
            postData.put("time", date);

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


}
