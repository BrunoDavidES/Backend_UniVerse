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
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response assignResponsible(EnrollData enrollData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key enrollKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Subject", enrollData.getSubjectId()))
                    .addAncestor(PathElement.of("User", enrollData.getUsername()))
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

                LOG.info("User " + enrollData.getUsername() + " assigned as " + enrollData.getRole() + " for subject " + enrollData.getSubjectId());
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
    @Path("/unassign")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unassignResponsible(EnrollData enrollData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key enrollKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Subject", enrollData.getSubjectId()))
                    .addAncestor(PathElement.of("User", enrollData.getUsername()))
                    .setKind("Enrollment").newKey(enrollData.getYear()+"#"+enrollData.getSemester());
            Entity enrollment = txn.get(enrollKey);

            if( enrollment == null ) {
                txn.rollback();
                LOG.warning("Enrollment doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Enrollment doesn't exist").build();
            } else {
                txn.delete(enrollKey);

                LOG.info("User " + enrollData.getUsername() + " unassigned from subject " + enrollData.getSubjectId());
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
    @Path("/enroll")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response enrollStudent(EnrollData enrollData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key enrollKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Subject", enrollData.getSubjectId()))
                    .addAncestor(PathElement.of("User", enrollData.getUsername()))
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

                LOG.info("User " + enrollData.getUsername() + " enrolled in subject " + enrollData.getSubjectId());
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
    @Path("/withdraw")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response withdrawStudent(EnrollData enrollData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key enrollKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Subject", enrollData.getSubjectId()))
                    .addAncestor(PathElement.of("User", enrollData.getUsername()))
                    .setKind("Enrollment").newKey(enrollData.getYear()+"#"+enrollData.getSemester());
            Entity enrollment = txn.get(enrollKey);

            if( enrollment == null ) {
                txn.rollback();
                LOG.warning("Enrollment doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Enrollment doesn't exist").build();
            } else {
                txn.delete(enrollKey);

                LOG.info("User " + enrollData.getUsername() + " withdraw from subject " + enrollData.getSubjectId());
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
    @Path("/addGrade")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addGrade(EnrollData enrollData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key enrollKey = datastore.newKeyFactory()
                    .addAncestor(PathElement.of("Subject", enrollData.getSubjectId()))
                    .addAncestor(PathElement.of("User", enrollData.getUsername()))
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

                LOG.info("User " + enrollData.getUsername() + " withdraw from subject " + enrollData.getSubjectId());
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
