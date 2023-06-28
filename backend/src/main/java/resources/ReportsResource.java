package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import util.ReportData;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Logger;

import static util.AuthToken.*;
import static util.Constants.*;

@Path("/reports")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ReportsResource {
    private static final Logger LOG = Logger.getLogger(ReportsResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postReports(@HeaderParam("Authorization") String token,  ReportData data){
        LOG.fine("Attempt to post report.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if(!data.validate()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key reportKey;
            Entity entry;
            String id;
            do {
                id = UUID.randomUUID().toString();
                reportKey = datastore.newKeyFactory().setKind(REPORT).newKey(id);
                entry = txn.get(reportKey);
            } while(entry != null);

            Entity.Builder builder = Entity.newBuilder(reportKey);

            builder.set("title", data.title)
                    .set("id", id)
                    .set("reporter", String.valueOf(decodedToken.getUid()).replaceAll("\"", ""))
                    .set("location", data.location)
                    .set("status", STATUS_UNSEEN)
                    .set("time_creation", Timestamp.now());

            entry = builder.build();
            txn.add(entry);

            LOG.info("Report registered " + id);
            txn.commit();
            return Response.ok(id).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }



    @POST
    @Path("/edit/{id}")
    public Response editReport(@HeaderParam("Authorization") String token, @PathParam("id") String id){
        LOG.fine("Attempt to edit report");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key eventKey = datastore.newKeyFactory().setKind(REPORT).newKey(id);
            Entity entry = txn.get(eventKey);

            if( entry == null ) {
                txn.rollback();
                LOG.warning(REPORT_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(REPORT_DOES_NOT_EXIST).build();
            } else if(!entry.getString("reporter").equals(String.valueOf(decodedToken.getUid()).replaceAll("\"", ""))) {
                txn.rollback();
                LOG.warning("Wrong author.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Wrong author.").build();
            }else {
                Entity.Builder builder = Entity.newBuilder(entry);

                builder.set("time_lastUpdated", Timestamp.now());

                Entity newEntry = builder.build();
                txn.update(newEntry);

                LOG.info( "Report registered id: " + id);
                txn.commit();
                return Response.ok(id).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/status/{id}/{status}")
    public Response statusReport(@HeaderParam("Authorization") String token, @PathParam("id") String id, @PathParam("status") String status){
        LOG.fine("Attempt to edit report");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key eventKey = datastore.newKeyFactory().setKind(REPORT).newKey(id);
            Entity entry = txn.get(eventKey);

            if( entry == null ) {
                txn.rollback();
                LOG.warning(REPORT_DOES_NOT_EXIST);
                return Response.status(Response.Status.BAD_REQUEST).entity(REPORT_DOES_NOT_EXIST).build();
            } else if(!String.valueOf(getRole(decodedToken)).replaceAll("\"", "").equals(BO)){
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }else {
                if(status == null || (!status.equals(STATUS_SEEN) && !status.equals(STATUS_RESOLVED))){
                    txn.rollback();
                    LOG.warning("Invalid status.");
                    return Response.status(Response.Status.BAD_REQUEST).entity("Invalid status.").build();
                }
                Entity.Builder builder = Entity.newBuilder(entry);

                builder.set("time_lastUpdated", Timestamp.now());
                builder.set("status", status);

                Entity newEntry = builder.build();
                txn.update(newEntry);

                LOG.info( "Report status has been altered.");
                txn.commit();
                return Response.ok(id).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryReports(@HeaderParam("Authorization") String token, @QueryParam("limit") int limit,
                                 @QueryParam("offset") int offset, Map<String, String> filters) {
        LOG.fine("Attempt to query reports.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(decodedToken.getUid()).replaceAll("\"", ""));
        Entity user = datastore.get(userKey);
        if(!user.getString(ROLE).equals(BO)){
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();

        }

        QueryResults<Entity> queryResults;

        StructuredQuery.CompositeFilter attributeFilter = null;
        if( filters == null){
            filters = new HashMap<>(1);
        }
        StructuredQuery.PropertyFilter propFilter;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            propFilter = StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue());

            if(attributeFilter == null) {
                attributeFilter = StructuredQuery.CompositeFilter.and(propFilter);
            } else {
                attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);
            }
        }

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(REPORT)
                .setFilter(attributeFilter)
                .setLimit(limit)
                .setOffset(offset)
                .build();

        queryResults = datastore.run(query);

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        LOG.info("Ides receber um query ó filho!");
        Gson g = new Gson();
        return Response.ok(g.toJson(results)).build();

    }

    @GET
    @Path("/unresolved")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response numberOfUnresolvedReports(@HeaderParam("Authorization") String token) {
        LOG.fine("Trying to know how many unresolved reports exist");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Key userKey = datastore.newKeyFactory().setKind(USER).newKey(String.valueOf(decodedToken.getUid()).replaceAll("\"", ""));
        Entity user = datastore.get(userKey);
        if(!user.getString(ROLE).equals(BO)){
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();

        }

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(REPORT)
                .setFilter(StructuredQuery.PropertyFilter.neq("status", STATUS_RESOLVED))
                .build();

        QueryResults<Entity> queryResults = datastore.run(query);

        int counter = 0;

        while (queryResults.hasNext()){
            counter++;
            queryResults.next();
        }

        return Response.ok(counter).build();

    }
}
