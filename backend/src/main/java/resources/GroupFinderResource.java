package resources;

import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import models.GroupFinderData;
import models.UserLocationData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static utils.FirebaseAuth.authenticateToken;

@Path("/{forumID}/groupFinder")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class GroupFinderResource {
    private static final Logger LOG = Logger.getLogger(GroupFinderResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public GroupFinderResource() {}

    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePermissions(@HeaderParam("Authorization") String token,
                                      @PathParam("forumID") String forumID,
                                      GroupFinderData data) {

        LOG.fine("Attempt to post group");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        try {
            DatabaseReference groupsRef = firebaseDatabase.getReference("forums")
                    .child(forumID)
                    .child("group_finder");
            String groupID = groupsRef.push().getKey();
            groupsRef.child(groupID).setValueAsync(data);

            LOG.info("Group posted to forum: " + forumID);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.info("Error posting group");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{groupID}/apply")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePermissions(@HeaderParam("Authorization") String token,
                                      @PathParam("forumID") String forumID,
                                      @PathParam("groupID") String groupID) {

        LOG.fine("Attempt to apply to group: " + groupID);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        try {
            Map<String, Object> applicationData = new HashMap<>();
            applicationData.put("username", decodedToken.getUid());
            applicationData.put("name", decodedToken.getName());

            DatabaseReference groupRef = firebaseDatabase.getReference("forums")
                    .child(forumID)
                    .child("group_finder")
                    .child(groupID)
                    .child("applications");
            String applicationID = groupRef.push().getKey();
            groupRef.child(applicationID).setValueAsync(decodedToken.getUid());

            LOG.info("Application posted to group: " + applicationID);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.info("Error posting group");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
