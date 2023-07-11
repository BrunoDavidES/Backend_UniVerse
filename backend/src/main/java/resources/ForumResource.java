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
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static resources.NotificationResource.*;
import static utils.Constants.*;
import static utils.FirebaseAuth.*;


/**
 * Resource class for managing forums.
 */
@Path("/forum")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ForumResource {
    private static final Logger LOG = Logger.getLogger(ForumResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    /**
     * Default constructor for the ForumResource class.
     */
    public ForumResource() {}


    /**
     * Creates a new forum.
     *
     * @param token The authorization token.
     * @param data  The forum data.
     * @return Response indicating the status of the forum creation.
     */
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
        return newForum(data, userID, userName);
    }

    /**
     * Deletes an existing forum.
     *
     * @param token    The authorization token.
     * @param forumID  The ID of the forum to delete.
     * @return Response indicating the status of the forum deletion.
     */
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

        return  deleteExistingForum(forumID, userID);
    }

    /**
     * Posts a message to a forum.
     *
     * @param token   The authorization token.
     * @param forumID The ID of the forum.
     * @param data    The forum data.
     * @return Response indicating the status of the message posting.
     */
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

        return postMessage(decodedToken, data, forumID);
    }

    /**
     * Edits a post in a forum.
     *
     * @param token   The authorization token.
     * @param forumID The ID of the forum.
     * @param postID  The ID of the post to edit.
     * @param data    The forum data.
     * @return Response indicating the status of the post editing.
     */
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

        return editMessage(decodedToken, data, forumID, postID);
    }


    /**
     * Deletes a post in a forum.
     *
     * @param token   The authorization token.
     * @param forumID The ID of the forum.
     * @param postID  The ID of the post to delete.
     * @return Response indicating the status of the post deletion.
     */
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

        return  deleteMessage(decodedToken, forumID, postID);
    }

    /**
     * Promotes a member in a forum.
     *
     * @param token    The authorization token.
     * @param forumID  The ID of the forum.
     * @param memberID The ID of the member to promote.
     * @return Response indicating the status of the member promotion.
     */
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

        return pMember(decodedToken, forumID, memberID);
    }

    /**
     * Demotes a member in a forum.
     *
     * @param token    The authorization token.
     * @param forumID  The ID of the forum.
     * @param memberID The ID of the member to demote.
     * @return Response indicating the status of the member demotion.
     */
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

       return dMember(decodedToken, forumID, memberID);
    }

    /**
     * Joins a forum.
     *
     * @param token   The authorization token.
     * @param forumID The ID of the forum to join.
     * @param data    The forum data.
     * @return Response indicating the status of the forum joining.
     */
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

       return joinChat(decodedToken, data, forumID);
    }

    /**
     * Leaves a forum.
     *
     * @param token   The authorization token.
     * @param forumID The ID of the forum to leave.
     * @return Response indicating the status of the forum leaving.
     */
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

       return leaveChat(decodedToken, forumID);
    }

    /**
     * Retrieves the current date and time in a formatted string.
     *
     * @return The current date and time formatted as "dd/MM/yyyy HH:mm".
     */
    private String getCurrentDate() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        return dateFormat.format(currentDate);
    }

    /**
     * Retrieves the role of a user in a forum.
     *
     * @param forumID The ID of the forum.
     * @param userID  The ID of the user.
     * @return The role of the user in the forum.
     */
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

    /**
     * Retrieves the author of a post in a forum.
     *
     * @param forumID The ID of the forum.
     * @param postID  The ID of the post.
     * @return The author of the post.
     */
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

    /**
     * Creates a new forum and associated data.
     *
     * @param data     The forum data.
     * @paramuserID    The ID of the user creating the forum.
     * @param userName The name of the user creating the forum.
     * @return Response indicating the status of the forum creation.
     */
    private Response newForum(ForumData data, String userID, String userName){
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

    /**
     * Deletes an existing forum and associated data.
     *
     * @param forumID The ID of the forum to delete.
     * @param userID  The ID of the user deleting the forum.
     * @return Response indicating the status of the forum deletion.
     */
    private Response deleteExistingForum(String forumID, String userID){
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

            QueryResults<Entity> results = txn.run(query);
            while (results.hasNext()) {
                Entity entity = results.next();
                txn.delete(entity.getKey());
                firebaseDatabase.getReference(USERS)
                        .child(entity.getKey().getName().replace(".", "-"))
                        .child(FORUMS)
                        .child(forumID)
                        .removeValueAsync();
            }

            query = Query.newEntityQueryBuilder()
                    .setKind(FORUM_POSTS)
                    .setFilter(StructuredQuery.PropertyFilter.hasAncestor(key))
                    .build();

            results = txn.run(query);
            while (results.hasNext()) {
                Entity entity = results.next();
                txn.delete(entity.getKey());
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

    /**
     * Posts a message to a forum.
     *
     * @param decodedToken The decoded authorization token.
     * @param data         The forum data.
     * @param forumID      The ID of the forum.
     * @return Response indicating the status of the message posting.
     */
    private Response postMessage(FirebaseToken decodedToken, ForumData data, String forumID){
        String userID = decodedToken.getUid();

        Transaction txn = datastore.newTransaction();
        try {
            String forumRole = getForumRole(forumID, userID);
            if (!forumRole.equals(ADMIN) && !forumRole.equals(ASSISTANT)) {
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
            }

            String date = getCurrentDate();

            DatabaseReference forumRef = firebaseDatabase.getReference(FORUMS).child(forumID).child("feed");
            String postID = forumRef.push().getKey();
            forumRef.child(postID).child("author").child("username").setValueAsync(decodedToken.getUid());
            forumRef.child(postID).child("author").child("name").setValueAsync(decodedToken.getName());
            forumRef.child(postID).child("message").setValueAsync(data.getMessage());
            forumRef.child(postID).child("posted").setValueAsync(date);

            Key key = datastore.newKeyFactory()
                    .setKind(FORUM_POSTS)
                    .addAncestors(PathElement.of(FORUM, forumID))
                    .newKey(postID);

            Entity post = Entity.newBuilder(key)
                    .set("author", userID)
                    .build();
            txn.add(post);

            LOG.info("Posted to forum");

            CompletableFuture.runAsync(() -> {
                Key fkey = datastore.newKeyFactory()
                        .setKind(FORUM)
                        .newKey(forumID);
                String forumName = txn.get(fkey).getString("name");

                Query<Entity> query = Query.newEntityQueryBuilder()
                        .setKind(USER_FORUMS)
                        .setFilter(StructuredQuery.PropertyFilter.hasAncestor(fkey))
                        .build();
                QueryResults<Entity> results = txn.run(query);

                List<String> members = new ArrayList<>();
                while (results.hasNext()) {
                    Entity entity = results.next();
                    members.add(entity.getKey().getName());
                }
                sendNotification(members, forumName);

                LOG.warning("Members notified");
            });
            txn.commit();

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

    /**
     * Edits a post in a forum.
     *
     * @param decodedToken The decoded authorization token.
     * @param data         The forum data.
     * @param forumID      The ID of the forum.
     * @param postID       The ID of the post to edit.
     * @return Response indicating the status of the post editing.
     */
    private Response editMessage(FirebaseToken decodedToken, ForumData data, String forumID, String postID){
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

    /**
     * Deletes a post in a forum.
     *
     * @param decodedToken The decoded authorization token.
     * @param forumID      The ID of the forum.
     * @param postID       The ID of the post to delete.
     * @return Response indicating the status of the post deletion.
     */
    private Response deleteMessage(FirebaseToken decodedToken, String forumID, String postID){
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

    /**
     * Promotes a member in a forum.
     *
     * @param decodedToken The decoded authorization token.
     * @param forumID      The ID of the forum.
     * @param memberID     The ID of the member to promote.
     * @return Response indicating the status of the member promotion.
     */
    private Response pMember(FirebaseToken decodedToken, String forumID, String memberID) {
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
            if (memberRole.equals("MEMBER")) {
                promotedRole = ASSISTANT;
            } else if (memberRole.equals(ASSISTANT)) {
                promotedRole = ADMIN;
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(USER_DOES_NOT_EXIST).build();
            }
            LOG.warning(promotedRole);

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
            LOG.warning(e.getMessage());
            LOG.warning(memberID);
            LOG.info("Error promoting member");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


    /**
     * Demotes a member in a forum.
     *
     * @param decodedToken The decoded authorization token.
     * @param forumID      The ID of the forum.
     * @param memberID     The ID of the member to demote.
     * @return Response indicating the status of the member demotion.
     */
    private Response dMember(FirebaseToken decodedToken, String forumID, String memberID){
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
            if(memberRole.equals(ASSISTANT)) {
                demotedRole = "MEMBER";
            } else if(memberRole.equals(ADMIN)) {
                demotedRole = ASSISTANT;
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

    /**
     * Joins a forum.
     *
     * @param decodedToken The decoded authorization token.
     * @param data         The forum data.
     * @param forumID      The ID of the forum to join.
     * @return Response indicating the status of the forum joining.
     */
    private Response joinChat(FirebaseToken decodedToken, ForumData data, String forumID){
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

    /**
     * Leaves a forum.
     *
     * @param decodedToken The decoded authorization token.
     * @param forumID      The ID of the forum to leave.
     * @return Response indicating the status of the forum leaving.
     */
    private Response leaveChat(FirebaseToken decodedToken, String forumID){
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
}
