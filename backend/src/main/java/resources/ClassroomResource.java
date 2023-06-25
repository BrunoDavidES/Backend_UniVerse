package resources;

import com.google.cloud.datastore.*;
import util.ClassroomData;
import util.ScheduleSlotData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
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
    @Path("/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeClassroom(@QueryParam("classroomId") String classroomId) {

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

    @POST
    @Path("/{classroomId}/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addScheduleSlot(@PathParam("classroomId") String classroomId, ScheduleSlotData scheduleSlotData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key scheduleSlotKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Classroom", classroomId))
                    .setKind("Classroom_Schedule")
                    .newKey(scheduleSlotData.getDayOfWeek()+"#"+scheduleSlotData.getStartTime());
            Entity scheduleSlot = txn.get(scheduleSlotKey);

            if( scheduleSlot != null ) {
                txn.rollback();
                LOG.warning("ScheduleSlot already exists");
                return Response.status(Response.Status.BAD_REQUEST).entity("ScheduleSlot already exists").build();
            } else {

                scheduleSlot = Entity.newBuilder(scheduleSlotKey)
                        .set("dayOfWeek", scheduleSlotData.getDayOfWeek())
                        .set("startTime", scheduleSlotData.getStartTime())
                        .set("endTime", scheduleSlotData.getEndTime())
                        .set("subject",scheduleSlotData.getSubject())
                        .build();
                txn.add(scheduleSlot);

                LOG.info("ScheduleSlot added to classroom " + classroomId);
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/{classroomId}/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeScheduleSlot(@PathParam("classroomId") String classroomId, @QueryParam("scheduleSlotId") String scheduleSlotId) {

        Transaction txn = datastore.newTransaction();
        try {
            Key scheduleSlotKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Classroom", classroomId))
                    .setKind("Classroom_Schedule")
                    .newKey(scheduleSlotId);
            Entity scheduleSlot = txn.get(scheduleSlotKey);

            if( scheduleSlot == null ) {
                txn.rollback();
                LOG.warning("ScheduleSlot doesn't exists");
                return Response.status(Response.Status.NOT_FOUND).entity("ScheduleSlot doesn't exists").build();
            } else {
                txn.delete(scheduleSlotKey);

                LOG.info("ScheduleSlot removed from classroom " + classroomId);
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
    @Path("/{classroomId}/directions")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response directionsClassroom(@PathParam("classroomId") String classroomId) {

        Transaction txn = datastore.newTransaction();
        try {
            Key classroomKey = datastore.newKeyFactory().setKind("Classroom").newKey(classroomId);
            Entity classroom = txn.get(classroomKey);

            if( classroom == null ) {
                txn.rollback();
                LOG.warning("Classroom doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Classroom doesn't exist").build();
            } else {
                List<String> directions = new ArrayList<>();
                directions.add(classroom.getString("building"));
                directions.add(classroom.getString("floor"));
                directions.add(classroom.getString("location"));

                LOG.info("Classroom directions retrieved " + classroomId);
                txn.commit();
                return Response.ok(directions).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @GET
    @Path("/{classroomId}/schedule")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response scheduleClassroom(@PathParam("classroomId") String classroomId) {

        Transaction txn = datastore.newTransaction();
        try {
            Key classroomKey = datastore.newKeyFactory().setKind("Classroom").newKey(classroomId);

            Query<Entity> query = Query.newEntityQueryBuilder()
                    .setKind("Classroom_Schedule")
                    .setFilter(StructuredQuery.PropertyFilter.hasAncestor(classroomKey))
                    .build();

            QueryResults<Entity> results = txn.run(query);

            List<Entity> schedule = new ArrayList<>();
            while (results.hasNext()) {
                Entity scheduleSlot = results.next();
                schedule.add(scheduleSlot);
            }

            return Response.ok(schedule).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


}
