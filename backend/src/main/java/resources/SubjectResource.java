package resources;

import com.google.cloud.datastore.*;
import util.EnrollData;
import util.SubjectData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/subject")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class SubjectResource {
    private static final Logger LOG = Logger.getLogger(SubjectResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addSubject(SubjectData subjectData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key subjectKey = datastore.newKeyFactory().setKind("Subject").newKey(subjectData.getId());
            Entity subject = txn.get(subjectKey);

            if( subject != null ) {
                txn.rollback();
                LOG.warning("Subject already exists");
                return Response.status(Response.Status.BAD_REQUEST).entity("Subject already exists").build();
            } else {

                subject = Entity.newBuilder(subjectKey)
                        .set("name", subjectData.getName())
                        .set("department", subjectData.getDepartment())
                        .build();
                txn.add(subject);

                LOG.info("Subject added in datastore " + subjectData.getId());
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
    public Response removeSubject(SubjectData subjectData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key subjectKey = datastore.newKeyFactory().setKind("Subject").newKey(subjectData.getId());
            Entity subject = txn.get(subjectKey);

            if( subject == null ) {
                txn.rollback();
                LOG.warning("Subject doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Subject doesn't exist").build();
            } else {
                txn.delete(subjectKey);

                LOG.info("Subject removed from datastore " + subjectData.getId());
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
    @Path("/{subjectId}/{userId}/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response assignResponsible(@PathParam("subjectId") String subjectId, @PathParam("userId") String userId, EnrollData enrollData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key enrollKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Subject", subjectId))
                    .addAncestor(PathElement.of("User", userId))
                    .setKind("Enrollment").newKey(enrollData.getYear()+"#"+enrollData.getSemester());
            Entity enrollment = txn.get(enrollKey);

            if( enrollment != null ) {
                txn.rollback();
                LOG.warning("Enrollment already exists");
                return Response.status(Response.Status.BAD_REQUEST).entity("Enrollment already exists").build();
            } else {

                enrollment = Entity.newBuilder(enrollKey)
                        .set("role", enrollData.getRole().toUpperCase())
                        .set("grade", "")
                        .build();
                txn.add(enrollment);

                LOG.info("User " + userId + " assigned as " + enrollData.getRole() + " for subject " + subjectId);
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
    @Path("/{subjectId}/{userId}/unassign")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unassignResponsible(@PathParam("subjectId") String subjectId, @PathParam("userId") String userId, EnrollData enrollData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key enrollKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Subject", subjectId))
                    .addAncestor(PathElement.of("User", userId))
                    .setKind("Enrollment").newKey(enrollData.getYear()+"#"+enrollData.getSemester());
            Entity enrollment = txn.get(enrollKey);

            if( enrollment == null ) {
                txn.rollback();
                LOG.warning("Enrollment doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Enrollment doesn't exist").build();
            } else {
                txn.delete(enrollKey);

                LOG.info("User " + userId + " unassigned from subject " + subjectId);
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
    @Path("/{subjectId}/{userId}/enroll")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response enrollStudent(@PathParam("subjectId") String subjectId, @PathParam("userId") String userId, EnrollData enrollData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key enrollKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Subject", subjectId))
                    .addAncestor(PathElement.of("User", userId))
                    .setKind("Enrollment").newKey(enrollData.getYear()+"#"+enrollData.getSemester());
            Entity enrollment = txn.get(enrollKey);

            if( enrollment != null ) {
                txn.rollback();
                LOG.warning("Enrollment already exists");
                return Response.status(Response.Status.BAD_REQUEST).entity("Enrollment already exists").build();
            } else {

                enrollment = Entity.newBuilder(enrollKey)
                        .set("role", "STUDENT")
                        .set("grade", "")
                        .build();
                txn.add(enrollment);

                LOG.info("User " + userId + " enrolled in subject " + subjectId);
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
    @Path("/{subjectId}/{userId}/withdraw")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response withdrawStudent(@PathParam("subjectId") String subjectId, @PathParam("userId") String userId, EnrollData enrollData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key enrollKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Subject", subjectId))
                    .addAncestor(PathElement.of("User", userId))
                    .setKind("Enrollment").newKey(enrollData.getYear()+"#"+enrollData.getSemester());
            Entity enrollment = txn.get(enrollKey);

            if( enrollment == null ) {
                txn.rollback();
                LOG.warning("Enrollment doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Enrollment doesn't exist").build();
            } else {
                txn.delete(enrollKey);

                LOG.info("User " + userId + " withdraw from subject " + subjectId);
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @PUT
    @Path("/{subjectId}/{userId}/addGrade")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addGrade(@PathParam("subjectId") String subjectId, @PathParam("userId") String userId, EnrollData enrollData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key enrollKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Subject", subjectId))
                    .addAncestor(PathElement.of("User", userId))
                    .setKind("Enrollment").newKey(enrollData.getYear()+"#"+enrollData.getSemester());
            Entity enrollment = txn.get(enrollKey);

            if( enrollment == null ) {
                txn.rollback();
                LOG.warning("Enrollment doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Enrollment doesn't exist").build();
            } else {
                enrollment = Entity.newBuilder(enrollKey, enrollment)
                        .set("grade", enrollData.getGrade())
                        .build();

                txn.put(enrollment);

                LOG.info("User " + userId + " withdraw from subject " + subjectId);
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


}
