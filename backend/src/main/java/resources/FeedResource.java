package resources;


import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.datastore.v1.CompositeFilter;
import util.FeedData;

import com.google.gson.Gson;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postFeed(FeedData data, String kind){

        /*
         * verificações de role e tokens
         */
        LOG.fine("Attempt to post feed.");

        if((!kind.equals("News") && !kind.equals("Event")) || !data.validate(kind)) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
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
            } while(entry != null);

            Entity.Builder builder = Entity.newBuilder(feedKey);

            builder.set("title", data.title)
                    .set("time_creation", Timestamp.now());

            entry = builder.build();
            txn.add(entry);

            LOG.info(kind + " registered " + id);
            txn.commit();
            return Response.ok(entry).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @PATCH
    @Path("/edit/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editFeed(@PathParam("id") String id, FeedData data, String kind){

        /*
         * verificações de role e tokens
         */
        LOG.fine("Attempt to add event.");

        if((!kind.equals("News") && !kind.equals("Event")) || !data.validate(kind)) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            if( entry == null ) {
                txn.rollback();
                LOG.warning(kind + " does not exist");
                return Response.status(Response.Status.BAD_REQUEST).entity(kind + " does not exist").build();
            } else {
                Entity.Builder builder = Entity.newBuilder(eventKey);

                builder.set("title", data.title)
                        .set("time_creation", Timestamp.now());

                entry = builder.build();
                txn.add(entry);

                LOG.info(kind + " registered " + data.title + "; id: " + id);
                txn.commit();
                return Response.ok(entry).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/delete/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteEvent(@PathParam("id") String id, String kind){

        /*
         * verificações de role e tokens
         */
        LOG.fine("Attempt to add event.");

        /*if((!kind.equals("News") && !kind.equals("Event")) || !data.validate(kind)) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }*/

        Transaction txn = datastore.newTransaction();

        try {
            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            if( entry == null ) {
                txn.rollback();
                LOG.warning(kind + " does not exist");
                return Response.status(Response.Status.BAD_REQUEST).entity(kind + " does not exist").build();
            } else {
                txn.delete(eventKey);
                LOG.info(kind + " registered " + id);
                txn.commit();
                return Response.ok(entry).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @GET
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryEvents(@QueryParam("kind") String kind, @QueryParam("filters") String[][] filters,
                                @QueryParam("limit") int limit, @QueryParam("offset") int offset){
        /*
         * verificações de role e tokens
         */
        LOG.fine("Attempt to query events.");

        QueryResults<Entity> queryResults;

        StructuredQuery.CompositeFilter attributeFilter = null;

        for (String[] filter: filters){
            StructuredQuery.PropertyFilter propFilter = StructuredQuery.PropertyFilter.eq(filter[0], filter[1]);

            if(attributeFilter == null)
                attributeFilter = StructuredQuery.CompositeFilter.and(propFilter);
            else
                attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);
        }

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(kind)
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
