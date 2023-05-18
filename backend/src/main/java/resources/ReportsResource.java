package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import util.ReportData;
import util.ValToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/reports")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ReportsResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postReports(@Context HttpServletRequest request,  ReportData data){
        LOG.fine("Attempt to post report.");



        if(!data.validate()) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        Transaction txn = datastore.newTransaction();

        try {

            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
            }

            Key reportKey;
            Entity entry;
            String id;
            do {
                id = UUID.randomUUID().toString();
                reportKey = datastore.newKeyFactory().setKind("Report").newKey(id);
                entry = txn.get(reportKey);
            } while(entry != null);

            Entity.Builder builder = Entity.newBuilder(reportKey);

            builder.set("title", data.title)
                    .set("reporter", data.reporter)
                    .set("department", data.department)
                    .set("status", "UNSEEN")
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
    public Response editReport(@Context HttpServletRequest request, @PathParam("id") String id){
        LOG.fine("Attempt to edit report");

        Transaction txn = datastore.newTransaction();

        try {

            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
            }

            Key eventKey = datastore.newKeyFactory().setKind("Report").newKey(id);
            Entity entry = txn.get(eventKey);

            if( entry == null ) {
                txn.rollback();
                LOG.warning("Report does not exist");
                return Response.status(Response.Status.BAD_REQUEST).entity("Report does not exist").build();
            } else {
                Entity.Builder builder = Entity.newBuilder(eventKey);

                builder.set("time_lastUpdated", Timestamp.now());

                entry = builder.build();
                txn.add(entry);

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
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryReports(@Context HttpServletRequest request, @QueryParam("limit") int limit,
                                 @QueryParam("offset") int offset, Map<String, String> filters) {
        LOG.fine("Attempt to query reports.");

        final ValToken validator = new ValToken();
        DecodedJWT token = validator.checkToken(request);

        if (token == null) {
            LOG.warning("Token not found");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
        }

        QueryResults<Entity> queryResults;

        StructuredQuery.CompositeFilter attributeFilter = null;

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            StructuredQuery.PropertyFilter propFilter = StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue());

            if(attributeFilter == null) {
                attributeFilter = StructuredQuery.CompositeFilter.and(propFilter);
            } else {
                attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);
            }
        }

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("Report")
                .setFilter(attributeFilter)
                .setLimit(limit)
                .setOffset(offset)
                .build();

        queryResults = datastore.run(query);

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        LOG.info("Ides receber um query ó filho!");
        Gson g = new Gson();
        return Response.ok(g.toJson(results)).entity("Vos recebestes ganda query results maninho!!!").build();

    }
}
