package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import util.MessageData;
import util.ValToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/inbox")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class InboxResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    //private static final Datastore datastore = DatastoreOptions.newBuilder().setHost("localhost:8081").setProjectId("id").build().getService();

    public InboxResource() { }

    @GET
    @Path("/received")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getReceivedInbox(@Context HttpServletRequest request) {
        LOG.fine("Attempt to get user received inbox");

        final ValToken validator = new ValToken();
        String codedToken = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    codedToken = cookie.getValue();
                    break;
                }
            }
        }

        if (codedToken == null) {
            LOG.warning("Token not found");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
        }

        try {
            DecodedJWT token = validator.validateToken(codedToken);
            String username = token.getClaim("user").asString();
            Transaction txn = datastore.newTransaction();
            try {
                Query<Entity> query = Query.newEntityQueryBuilder()
                        .setKind("Message")
                        .setFilter(StructuredQuery.PropertyFilter.eq("receiver", username))
                        .build();

                QueryResults<Entity> results = datastore.run(query);
                List<MessageData> inbox = new ArrayList<>();
                while (results.hasNext()) {
                    Entity ds_message = results.next();
                    MessageData message = new MessageData();
                    message.sender = ds_message.getString("sender");
                    message.content = ds_message.getString("content");
                    message.time_sent = ds_message.getString("time_sent");
                    inbox.add(message);
                }

                LOG.info("User inbox received retrieved");
                Gson gson = new Gson();
                String json = gson.toJson(inbox);
                txn.commit();
                return Response.ok(json, MediaType.APPLICATION_JSON).build();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        } catch (InvalidParameterException e) {
            LOG.warning("Token is invalid: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Token is invalid").build();
        }
    }

    @GET
    @Path("/sent")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSentInbox(@Context HttpServletRequest request) {
        LOG.fine("Attempt to get user sent inbox");

        final ValToken validator = new ValToken();
        String codedToken = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    codedToken = cookie.getValue();
                    break;
                }
            }
        }

        if (codedToken == null) {
            LOG.warning("Token not found");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found in cookies").build();
        }

        try {
            DecodedJWT token = validator.validateToken(codedToken);
            String username = token.getClaim("user").asString();
            Transaction txn = datastore.newTransaction();
            try {
                Query<Entity> query = Query.newEntityQueryBuilder()
                        .setKind("Message")
                        .setFilter(StructuredQuery.PropertyFilter.eq("sender", username))
                        .build();

                QueryResults<Entity> results = datastore.run(query);
                List<MessageData> inbox = new ArrayList<>();
                while (results.hasNext()) {
                    Entity ds_message = results.next();
                    MessageData message = new MessageData();
                    message.receiver = ds_message.getString("receiver");
                    message.content = ds_message.getString("content");
                    message.time_sent = ds_message.getString("time_sent");
                    inbox.add(message);
                }

                LOG.info("User inbox sent retrieved");
                Gson gson = new Gson();
                String json = gson.toJson(inbox);
                txn.commit();
                return Response.ok(json, MediaType.APPLICATION_JSON).build();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        } catch (InvalidParameterException e) {
            LOG.warning("Token is invalid: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Token is invalid").build();
        }
    }

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendMessage(@Context HttpServletRequest request, MessageData data) {
        LOG.fine("Attempt to send message to: " + data.receiver);

        final ValToken validator = new ValToken();
        String codedToken = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    codedToken = cookie.getValue();
                    break;
                }
            }
        }

        if (codedToken == null) {
            LOG.warning("Token not found");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
        }

        try {
            DecodedJWT token = validator.validateToken(codedToken);
            String username = token.getClaim("user").asString();
            Transaction txn = datastore.newTransaction();
            try {
                Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.receiver);
                Entity target = txn.get(targetKey);

                if (target == null) {
                    txn.rollback();
                    LOG.warning("User does not exist");
                    return Response.status(Response.Status.BAD_REQUEST).entity("User does not exist.").build();
                }

                Key messageKey = datastore.newKeyFactory().setKind("Message").newKey(username+data.receiver+data.time_sent);
                Entity message = Entity.newBuilder(messageKey)
                        .set("sender", username)
                        .set("receiver", data.receiver)
                        .set("content", data.content)
                        .set("time_sent", data.time_sent)
                        .build();
                txn.add(message);

                LOG.info("Message sent to: " + data.receiver);
                txn.commit();
                return Response.ok(message).build();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        } catch (InvalidParameterException e) {
            LOG.warning("Token is invalid: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Token is invalid").build();
        }
    }
}
