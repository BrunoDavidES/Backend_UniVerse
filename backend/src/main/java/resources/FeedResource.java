package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.*;
import com.google.firebase.auth.FirebaseToken;
import models.FeedData;

import com.google.gson.Gson;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Logger;

import static utils.FirebaseAuth.*;
import static utils.Constants.*;
import static utils.Query.*;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedResource {
    private static final Logger LOG = Logger.getLogger(FeedResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public FeedResource() {}

    @POST
    @Path("/post/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postEntry(@HeaderParam("Authorization") String token,
                              @PathParam("kind") String kind, FeedData data) {

        LOG.fine("Attempt to post entry to feed.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if((!kind.equals(NEWS) && !kind.equals(EVENT)) || !data.validate(kind)) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        String role = getRole(decodedToken);
        String name = decodedToken.getName();
        String username = decodedToken.getUid();

        if (kind.equals(EVENT) && !role.equals(D) && !role.equals(BO)){
            LOG.warning("No permission to create an event.");
            return Response.status(Response.Status.FORBIDDEN).entity("No permission to create an event.").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key feedKey;
            Entity entry;
            String id;
            do {
                id = UUID.randomUUID().toString();
                feedKey = datastore.newKeyFactory().setKind(kind).newKey(id);
                entry = txn.get(feedKey);
            } while (entry != null);

            Entity.Builder builder = Entity.newBuilder(feedKey)
                    .set("id", id)
                    .set("title", data.title)
                    .set("authorUsername", username)
                    .set("validated_backoffice", "false")
                    .set("time_creation", Timestamp.now());

            if (kind.equals(EVENT)) {
                builder.set("startDate", data.startDate)
                        .set("endDate", data.endDate)
                        .set("location", data.location)
                        .set("department", data.department)
                        .set("isPublic", data.isPublic)
                        .set("capacity", data.capacity)
                        .set("isItPaid", data.isItPaid);
            } else if (role.equals(BO) && data.authorNameByBO != null && !data.authorNameByBO.equals("")) {
                builder.set("authorName", data.authorNameByBO);
            } else {
                builder.set("authorName", name);
            }
            entry = builder.build();
            txn.add(entry);
            txn.commit();

            LOG.info(kind + " posted " + data.title + "; id: " + id);
            return Response.ok(id).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }

    }

    @PATCH
    @Path("/edit/{kind}/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editEntry(@HeaderParam("Authorization") String token,
                              @PathParam("kind") String kind,
                              @PathParam("id") String id, FeedData data) {

        LOG.fine("Attempt to edit feed entry.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if(!kind.equals(NEWS) && !kind.equals(EVENT)) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            String role = getRole(decodedToken);
            String username = decodedToken.getUid();

            if( entry == null ) {
                txn.rollback();
                LOG.warning(kind + " does not exist " + id);
                return Response.status(Response.Status.BAD_REQUEST).entity(kind + " does not exist " + id).build();
            }
            if (!data.validateEdit(entry, kind)) {
                txn.rollback();
                LOG.warning("Invalid request for editEntry");
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request").build();
            }
            if( !(entry.getString("authorUsername").equals(username) || role.equals(BO)) ) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
            }

            Entity.Builder newEntry = Entity.newBuilder(entry)
                    .set("title", data.title)
                    .set("time_lastupdated", Timestamp.now());

            if (kind.equals(EVENT)) {
                newEntry.set("startDate", data.startDate)
                        .set("endDate", data.endDate)
                        .set("location", data.location)
                        .set("department", data.department)
                        .set("isPublic", data.isPublic)
                        .set("capacity", data.capacity)
                        .set("isItPaid", data.isItPaid)
                        .set("validated_backoffice", data.validated_backoffice);
            }
            Entity updatedEntry = newEntry.build();
            txn.update(updatedEntry);
            txn.commit();

            LOG.info(kind + " edited " + data.title + "; id: " + id);
            return Response.ok().build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/delete/{kind}/{id}")
    public Response deleteEntry(@HeaderParam("Authorization") String token,
                                @PathParam("kind") String kind,
                                @PathParam("id") String id) {

        LOG.fine("Attempt to delete event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if((!kind.equals(NEWS) && !kind.equals(EVENT))) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            String role = getRole(decodedToken);
            String username = decodedToken.getUid();

            if( entry == null ) {
                txn.rollback();
                LOG.warning(kind + " does not exist");
                return Response.status(Response.Status.BAD_REQUEST).entity(kind + " does not exist").build();
            }
            if( !(entry.getString("authorUsername").equals(username) || role.equals(BO)) ) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
            }

            txn.delete(eventKey);
            txn.commit();

            LOG.info(kind + " deleted " + id);
            return Response.ok(entry).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/query/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryEntries(@HeaderParam("Authorization") String token,
                                 @PathParam("kind") String kind,
                                 @QueryParam("limit") String limit,
                                 @QueryParam("offset") String offset,
                                 Map<String, String> filters) {

        LOG.fine("Attempt to query feed " + kind);

        filterPublicEvents(token, kind, filters);

        CompositeFilter attributeFilter = CompositeFilterAnd(filters);

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(attributeFilter)
                .setLimit(Integer.parseInt(limit))
                .setOffset(Integer.parseInt(offset))
                .build();
        QueryResults<Entity> queryResults = datastore.run(query);

        LOG.info("Ides receber um query ó filho!");
        return Response.ok(toJson(queryResults)).build();
    }

    @POST
    @Path("/numberOf/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryEntriesNum(@HeaderParam("Authorization") String token,
                                    @PathParam("kind") String kind,
                                    Map<String, String> filters) {

        LOG.fine("Attempt to count the query feed " + kind);

        filterPublicEvents(token, kind, filters);

        CompositeFilter attributeFilter = CompositeFilterAnd(filters);

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(attributeFilter)
                .build();
        QueryResults<Entity> queryResults = datastore.run(query);

        LOG.info("Received a query!");

        int count = count(queryResults);

        LOG.info("Query feed counted");
        return Response.ok(count).build();
    }

    private void filterPublicEvents(String token, String kind, Map<String, String> filters) {
        if(kind.equals(EVENT)) {
            FirebaseToken decodedToken = authenticateToken(token);
            if (decodedToken == null) {
                LOG.info(TOKEN_NOT_FOUND);
                filters.put("isPublic", "yes");
            }
        }
    }


}
