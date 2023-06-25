package resources;

import com.google.cloud.datastore.*;
import util.ClassroomData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/classroom")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ClassroomResource {
    private static final Logger LOG = Logger.getLogger(ClassroomResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addClassroom(ClassroomData classroomData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key classroomKey = datastore.newKeyFactory().setKind("Classroom").newKey(classroomData.getId());
            Entity classroom = txn.get(classroomKey);

            if( classroom != null ) {
                txn.rollback();
                LOG.warning("Classroom already exists");
                return Response.status(Response.Status.BAD_REQUEST).entity("Classroom already exists").build();
            } else {

                classroom = Entity.newBuilder(classroomKey)
                        .set("building", classroomData.getBuilding())
                        .set("floor", classroomData.getFloor())
                        .set("location", classroomData.getLocation())
                        .set("schedule", classroomData.getSchedule())
                        .build();
                txn.add(classroom);

                LOG.info("Classroom added in datastore " + classroomData.getId());
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/remove/{classroomId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeClassroom(@PathParam("classroomId") String classroomId) {

        Transaction txn = datastore.newTransaction();
        try {
            Key classroomKey = datastore.newKeyFactory().setKind("Classroom").newKey(classroomId);
            Entity classroom = txn.get(classroomKey);

            if( classroom == null ) {
                txn.rollback();
                LOG.warning("Classroom doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Classroom doesn't exist").build();
            } else {

                txn.delete(classroomKey);

                LOG.info("Classroom removed from datastore " + classroomId);
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @GET
    @Path("/location/{classroomId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response locationClassroom(@PathParam("classroomId") String classroomId) {

        Transaction txn = datastore.newTransaction();
        try {
            Key classroomKey = datastore.newKeyFactory().setKind("Classroom").newKey(classroomId);
            Entity classroom = txn.get(classroomKey);

            if( classroom == null ) {
                txn.rollback();
                LOG.warning("Classroom doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Classroom doesn't exist").build();
            } else {

                String location = classroom.getString("location");

                LOG.info("Classroom location retrieved " + classroomId);
                txn.commit();
                return Response.ok(location).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @GET
    @Path("/schedule/{classroomId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response scheduleClassroom(@PathParam("classroomId") String classroomId) {

        Transaction txn = datastore.newTransaction();
        try {
            Key classroomKey = datastore.newKeyFactory().setKind("Classroom").newKey(classroomId);
            Entity classroom = txn.get(classroomKey);

            if( classroom == null ) {
                txn.rollback();
                LOG.warning("Classroom doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Classroom doesn't exist").build();
            } else {

                String schedule = classroom.getString("schedule");

                LOG.info("Classroom schedule retrieved " + classroomId);
                txn.commit();
                return Response.ok(schedule).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


}
