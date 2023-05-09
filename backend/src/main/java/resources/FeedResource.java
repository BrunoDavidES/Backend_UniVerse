package resources;


import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import util.FeedData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    //private static final Datastore datastore = DatastoreOptions.newBuilder().setHost("localhost:8081").setProjectId("id").build().getService();


    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postFeed(FeedData data, String kind){

        /*
         * verificações de role e tokens
         */
        LOG.fine("Attempt to add event: ");

        if((!kind.equals("News") && !kind.equals("Event")) || !data.validate(kind)) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key eventKey;
            Entity entry;
            String id;
            do {
                id = UUID.randomUUID().toString();
                eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
                entry = txn.get(eventKey);
            } while(entry != null);

            Entity.Builder builder = Entity.newBuilder(eventKey);

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
        LOG.fine("Attempt to add event: ");

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
                Entity.Builder builder = Entity.newBuilder(eventKey);

                builder.set("title", data.title)
                        .set("time_creation", Timestamp.now());

                entry = builder.build();
                txn.add(entry);

                LOG.info(kind + " registered " + data.eventname);
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
        LOG.fine("Attempt to add event: ");

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
    @Path("/queryEvents")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryEvents(){
        /*
         * verificações de role e tokens
         */
        return Response.ok().build();
    }

    @GET
    @Path("/queryNews")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryNews(){
        /*
         * verificações de role e tokens
         */
        return Response.ok().build();
    }

}
