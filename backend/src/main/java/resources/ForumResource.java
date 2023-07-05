package resources;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.Transaction;
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        if(!data.validateCreation()) {
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
            String date = getCurrentDate();

            Map<String, Object> memberData = new HashMap<>();
            memberData.put("name", userName);
            memberData.put(ROLE, ADMIN);
            memberData.put("joined", date);

            DatabaseReference forumsRef = firebaseDatabase.getReference("forums");
            String forumID = forumsRef.push().getKey();
            forumsRef.child(forumID).child("name").setValueAsync(data.getName());
            forumsRef.child(forumID).child("creation").setValueAsync(date);
            forumsRef.child(forumID).child("members").child(userID.replace(".", "-")).setValueAsync(memberData);

            memberData.replace("name", data.getName());

            firebaseDatabase.getReference(USERS)
                    .child(userID.replace(".", "-"))
                    .child(FORUMS)
                    .child(forumID)
                    .setValueAsync(memberData);

            Key forumKey = datastore.newKeyFactory().setKind(FORUM).newKey(forumID);

            Entity forum = Entity.newBuilder(forumKey)
                    .set("name", data.getName())
                    .set("password", data.getPassword())
                    .build();
            txn.add(forum);

            Key userForumsKey = datastore.newKeyFactory()
                    .setKind(USER_FORUMS)
                    .addAncestors(PathElement.of(FORUM, forumID))
                    .newKey(userID);

            Entity userForums = Entity.newBuilder(userForumsKey)
                    .set(ROLE, ADMIN)
                    .build();
            txn.add(userForums);
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
    public Response deleteForum(@HeaderParam("Authorization") String token,
                                @PathParam("forumID") String forumID) {

        LOG.fine("Attempt to delete forum: " + forumID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        String userID = decodedToken.getUid();

        Transaction txn = datastore.newTransaction();
        try {
            String forumRole = getForumRole(forumID, userID);

            if (!forumRole.equals(ADMIN)) {
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
            }

            firebaseDatabase.getReference(FORUMS).child(forumID).removeValueAsync();

            Key key = datastore.newKeyFactory()
                    .setKind(FORUM)
                    .newKey(forumID);

            Query<Entity> query = Query.newEntityQueryBuilder()
                    .setKind(USER_FORUMS)
                    .setFilter(StructuredQuery.PropertyFilter.hasAncestor(key))
                    .build();

            QueryResults<Entity> results = datastore.run(query);
            while (results.hasNext()) {
                Entity entity = results.next();
                datastore.delete(entity.getKey());
                firebaseDatabase.getReference(USERS)
                        .child(entity.getKey().toString().replace(".", "-"))
                        .child(FORUMS)
                        .child(forumID)
                        .removeValueAsync();
            }

            query = Query.newEntityQueryBuilder()
                    .setKind(USER_FORUMS)
                    .setFilter(StructuredQuery.PropertyFilter.hasAncestor(key))
                    .build();

            results = datastore.run(query);
            while (results.hasNext()) {
                Entity entity = results.next();
                datastore.delete(entity.getKey());
            }

            key = datastore.newKeyFactory().setKind(FORUM).newKey(forumID);
            txn.delete(key);
            txn.commit();

            LOG.info("Forum deleted");
            return Response.ok(forumID).build();
        } catch (Exception e) {
            txn.rollback();
            LOG.info("Error deleting forum");
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
    public Response postMessage(@HeaderParam("Authorization") String token,
                                @PathParam("forumID") String forumID,
                                ForumData data) {

        LOG.fine("Attempt to post to forum: " + data.getName());

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        String userID = decodedToken.getUid();

        Transaction txn = datastore.newTransaction();
        try {
            String forumRole = getForumRole(forumID, userID);
            if (!forumRole.equals(ADMIN) && !forumRole.equals(ASSISTANT)) {
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
            }

            String date = getCurrentDate();

            Map<String, Object> postData = new HashMap<>();
            postData.put("author", decodedToken.getName());
            postData.put("message", data.getMessage());
            postData.put("posted", date);

            DatabaseReference forumRef = firebaseDatabase.getReference(FORUMS).child(forumID).child("feed");
            String postID = forumRef.push().getKey();
            forumRef.child(postID).setValueAsync(postData);

            Key key = datastore.newKeyFactory()
                    .setKind(FORUM_POSTS)
                    .addAncestors(PathElement.of(FORUM, forumID))
                    .newKey(postID);

            Entity post = Entity.newBuilder(key)
                    .set("author", userID)
                    .build();
            txn.add(post);
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

        try {
            String author = getPostAuthor(forumID, postID);

            if(!userID.equals(author)) {
                LOG.warning("Author: " + author);
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
            }

            String date = getCurrentDate();

            DatabaseReference postRef = firebaseDatabase.getReference(FORUMS)
                    .child(forumID)
                    .child("feed")
                    .child(postID);
            postRef.child("message").setValueAsync(data.getMessage());
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
    public Response deletePost(@HeaderParam("Authorization") String token,
                               @PathParam("forumID") String forumID,
                               @PathParam("postID") String postID) {

        LOG.fine("Attempt to remove post: " + postID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        String userID = decodedToken.getUid();

        Transaction txn = datastore.newTransaction();
        try {
            String forumRole = getForumRole(forumID, userID);
            String author = getPostAuthor(forumID, postID);

            if(!userID.equals(author) || !forumRole.equals(ADMIN)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
            }

            Key key = datastore.newKeyFactory()
                    .setKind(FORUM_POSTS)
                    .addAncestors(PathElement.of(FORUM, forumID))
                    .newKey(postID);
            Entity entity = txn.get(key);

            if(entity == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(TOKEN_NOT_FOUND).build();
            }
            txn.delete();
            txn.commit();

            firebaseDatabase.getReference(FORUMS)
                    .child(forumID)
                    .child("feed")
                    .child(postID)
                    .removeValueAsync();

            LOG.info("Removed post");
            return Response.ok(author).build();
        } catch (Exception e) {
            txn.rollback();
            LOG.info("Error removing post");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        String userID = decodedToken.getUid();

        Transaction txn = datastore.newTransaction();
        try {
            String userRole = getForumRole(forumID, userID);

            Key key = datastore.newKeyFactory()
                    .setKind(USER_FORUMS)
                    .addAncestors(PathElement.of(FORUM, forumID))
                    .newKey(memberID);
            Entity entity = txn.get(key);
            String memberRole = entity.getString("role");

            if (!userRole.equals(ADMIN)) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            String promotedRole;
            if(memberRole.equals("MEMBER")) {
                promotedRole = ASSISTANT;
            } else if(memberRole.equals(ASSISTANT)) {
                promotedRole = ADMIN;
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(USER_DOES_NOT_EXIST).build();
            }

            firebaseDatabase.getReference(FORUMS)
                    .child(forumID)
                    .child("members")
                    .child(memberID.replace(".", "-"))
                    .child(ROLE)
                    .setValueAsync(promotedRole);

            firebaseDatabase.getReference(USERS)
                    .child(memberID.replace(".", "-"))
                    .child(FORUMS)
                    .child(forumID)
                    .child(ROLE)
                    .setValueAsync(promotedRole);

            entity = Entity.newBuilder(entity)
                    .set(ROLE, promotedRole)
                    .build();
            txn.put(entity);
            txn.commit();

            LOG.info("Member promoted");
            return Response.ok(userRole).build();
        } catch (Exception e) {
            txn.rollback();
            LOG.info("Error promoting member");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        String userID = decodedToken.getUid();

        Transaction txn = datastore.newTransaction();
        try {
            String userRole = getForumRole(forumID, userID);

            Key key = datastore.newKeyFactory()
                    .setKind(USER_FORUMS)
                    .addAncestors(PathElement.of(FORUM, forumID))
                    .newKey(memberID);
            Entity entity = txn.get(key);
            String memberRole = entity.getString("role");

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

            firebaseDatabase.getReference(FORUMS)
                    .child(forumID)
                    .child("members")
                    .child(memberID.replace(".", "-"))
                    .child(ROLE)
                    .setValueAsync(demotedRole);

            firebaseDatabase.getReference(USERS)
                    .child(memberID.replace(".", "-"))
                    .child(FORUMS)
                    .child(forumID)
                    .child(ROLE)
                    .setValueAsync(demotedRole);

            entity = Entity.newBuilder(entity)
                    .set(ROLE, demotedRole)
                    .build();
            txn.put(entity);
            txn.commit();

            LOG.info("Member demoted");
            return Response.ok(userRole).build();
        } catch (Exception e) {
            txn.rollback();
            LOG.info("Error demoting member");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        String userID = decodedToken.getUid();
        String userName = decodedToken.getName();

        Transaction txn = datastore.newTransaction();
        try {
            Key forumKey = datastore.newKeyFactory().setKind(FORUM).newKey(forumID);

            Entity forum = txn.get(forumKey);
            if(forum == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Incorrect Credentials").build();
            }

            if(!data.getPassword().equals(forum.getString("password"))) {
                txn.rollback();
                return Response.status(Response.Status.FORBIDDEN).entity("Incorrect Credentials").build();
            }

            String date = getCurrentDate();

            Map<String, Object> memberData = new HashMap<>();
            memberData.put("name", userName);
            memberData.put(ROLE, "MEMBER");
            memberData.put("joined", date);

            firebaseDatabase.getReference(FORUMS)
                    .child(forumID)
                    .child("members")
                    .child(userID.replace(".", "-"))
                    .setValueAsync(memberData);

            memberData.replace("name", forum.getString("name"));

            firebaseDatabase.getReference(USERS)
                    .child(userID.replace(".", "-"))
                    .child(FORUMS)
                    .child(forumID)
                    .setValueAsync(memberData);

            Key key = datastore.newKeyFactory()
                    .setKind(USER_FORUMS)
                    .addAncestors(PathElement.of(FORUM, forumID))
                    .newKey(userID);

            Entity userForums = Entity.newBuilder(key)
                    .set(ROLE, "MEMBER")
                    .build();
            txn.add(userForums);
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

    @DELETE
    @Path("/{forumID}/leave")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response leaveForum(@HeaderParam("Authorization") String token,
                              @PathParam("forumID") String forumID) {

        LOG.fine("Attempt to leave forum: " + forumID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TOKEN_NOT_FOUND).build();
        }

        String userID = decodedToken.getUid();

        Transaction txn = datastore.newTransaction();
        try {
            Key key = datastore.newKeyFactory()
                    .setKind(USER_FORUMS)
                    .addAncestors(PathElement.of(FORUM, forumID))
                    .newKey(userID);
            Entity entity = txn.get(key);

            if(entity == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(TOKEN_NOT_FOUND).build();
            }
            txn.delete();
            txn.commit();

            firebaseDatabase.getReference(FORUMS)
                    .child(forumID).child("members")
                    .child(userID.replace(".", "-"))
                    .removeValueAsync();

            firebaseDatabase.getReference(USERS)
                    .child(userID.replace(".", "-"))
                    .child(FORUMS)
                    .child(forumID)
                    .removeValueAsync();

            LOG.info("Member left forum");
            return Response.ok(forumID).build();
        } catch (Exception e) {
            txn.rollback();
            LOG.info("Error leaving forum");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    private String getCurrentDate() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        return dateFormat.format(currentDate);
    }

    private String getForumRole (String forumID, String userID) {
        Key key = datastore.newKeyFactory()
                .setKind(USER_FORUMS)
                .addAncestors(PathElement.of(FORUM, forumID))
                .newKey(userID);
        Entity entity = datastore.get(key);

        if(entity == null) {
            return "";
        }

        return datastore.get(key).getString(ROLE);
    }

    private String getPostAuthor (String forumID, String postID) {
        Key key = datastore.newKeyFactory()
                .setKind(FORUM_POSTS)
                .addAncestors(PathElement.of(FORUM, forumID))
                .newKey(postID);
        Entity entity = datastore.get(key);

        if(entity == null) {
            return "";
        }

        return datastore.get(key).getString("author");
    }


}
