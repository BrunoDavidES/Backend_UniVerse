package resources;


import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import util.EventData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/staticEntities")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EventsAndNewsResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    //private static final Datastore datastore = DatastoreOptions.newBuilder().setHost("localhost:8081").setProjectId("id").build().getService();



    @POST
    @Path("/addEvent")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postEvent( EventData data){

        /*
         * verificações de role e tokens
         */
        LOG.fine("Attempt to add event: ");

        if( !data.validateEvent() ) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key eventKey = datastore.newKeyFactory().setKind("Event").newKey(data.eventname);
            //Key roleKey = datastore.newKeyFactory().setKind("RolesMap").newKey("ROLES MAP"); // MAP ENTITY TO FINISH
            Entity event = txn.get(eventKey);
            //Entity roleAttributes = txn.get(mapKey); //MAP ENTITY TO FINISH

            if( event != null ) {
                txn.rollback();
                LOG.warning("Event already exists");
                return Response.status(Response.Status.BAD_REQUEST).entity("Event already exists").build();
            } else {
                Entity.Builder builder = Entity.newBuilder(eventKey);

                builder.set("name", data.name)
                        .set("time_creation", Timestamp.now());

                event = builder.build();
                txn.add(event);

                LOG.info("User registered " + data.eventname);
                txn.commit();
                return Response.ok(event).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/addNews")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNews(){
        /*
         * verificações de role e tokens
         */
        return Response.ok().build();
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

    @PATCH
    @Path("/editEvent")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editEvent(){
        /*
         * verificações de role e tokens
         */
        return Response.ok().build();
    }

    @PATCH
    @Path("/editNews")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editNews(){
        /*
         * verificações de role e tokens
         */
        return Response.ok().build();
    }

    @DELETE
    @Path("/deleteEvent")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteEvent(){
        /*
         * verificações de role e tokens
         */
        return Response.ok().build();
    }

    @DELETE
    @Path("/deleteNews")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteNews(){
        /*
         * verificações de role e tokens
         */
        return Response.ok().build();
    }

}
