package resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import models.DepartmentData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static utils.FirebaseAuth.*;
import static utils.Constants.*;

@Path("/department")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DepartmentResource {
    private static final Logger LOG = Logger.getLogger(DepartmentResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerDepartment(@HeaderParam("Authorization") String token,
                                       DepartmentData data) {
        LOG.fine("Attempt to register department: " + data.id);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if( !data.validateRegister() ) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }
        if(!getRole(decodedToken).equals(BO)) {
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key presidentKey = datastore.newKeyFactory().setKind(USER).newKey(data.president);
            Entity president = txn.get(presidentKey);

            if( president == null ) {
                txn.rollback();
                LOG.warning(WRONG_PRESIDENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PRESIDENT).build();
            }

            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.id);
            Entity department = txn.get(departmentKey);

            if( department != null ) {
                txn.rollback();
                LOG.warning(DEPARTMENT_ALREADY_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(DEPARTMENT_ALREADY_EXISTS).build();
            }

            department = Entity.newBuilder(departmentKey)
                    .set("id", data.id)
                    .set("email", data.email)
                    .set("name", data.name)
                    .set("president", data.president)
                    .set("phone_number", data.phoneNumber)
                    .set("address", data.address)
                    .set("fax", data.fax)
                    .set("members_list", "")
                    .set("time_creation", Timestamp.now())
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.add(department);
            txn.commit();

            LOG.info("Department registered " + data.id);
            return Response.ok(department).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyDepartment(@HeaderParam("Authorization") String token,
                                     DepartmentData data){
        LOG.fine("Attempt to modify department.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if( !data.validateModify()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }
        if(!getRole(decodedToken).equals(BO)) {
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key presidentKey = datastore.newKeyFactory().setKind(USER).newKey(data.president);
            Entity president = txn.get(presidentKey);

            if( president == null ) {
                txn.rollback();
                LOG.warning(WRONG_PRESIDENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PRESIDENT).build();
            }

            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.id);
            Entity department = txn.get(departmentKey);

            if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            }

            data.fillGaps(department);
            Entity updatedDepartment = Entity.newBuilder(department)
                    .set("email", data.email)
                    .set("name", data.name)
                    .set("president", data.president)
                    .set("phone_number", data.phoneNumber)
                    .set("address", data.address)
                    .set("fax", data.fax)
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.update(updatedDepartment);
            txn.commit();

            LOG.info(data.id + " edited.");
            return Response.ok(updatedDepartment).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/delete/{id}")
    public Response deleteDepartment(@HeaderParam("Authorization") String token,
                                     @PathParam("id") String id){
        LOG.fine("Attempt to delete department.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if(!getRole(decodedToken).equals(BO)) {
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(id);
            Entity department = txn.get(departmentKey);

            if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            }

            String list = department.getString("members_list");
            String userPersonalList;
            String[] attributes;
            Key memberKey;
            Entity memberEntity;
            Entity newUser;
            for(String valuesOfMember : list.split("#")) {
                if(!valuesOfMember.equals("")) {
                    attributes = valuesOfMember.split("%");

                    memberKey = datastore.newKeyFactory().setKind(USER).newKey(attributes[1]);
                    memberEntity = txn.get(memberKey);

                    if (memberEntity == null) {
                        txn.rollback();
                        LOG.warning(WRONG_MEMBER);
                        return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_MEMBER).build();
                    }

                    userPersonalList = memberEntity.getString("job_list");
                    userPersonalList = userPersonalList.replace("#" + department.getString("id") + "%" + attributes[0], "");
                    newUser = Entity.newBuilder(memberEntity)
                            .set("job_list", userPersonalList)
                            .set("time_lastupdate", Timestamp.now())
                            .build();

                    txn.update(newUser);
                }
            }
            txn.delete(departmentKey);
            txn.commit();

            LOG.info("Department deleted.");
            return Response.ok(department).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryDepartment(@HeaderParam("Authorization") String token,
                                    @QueryParam("limit") String limit,
                                    @QueryParam("offset") String offset, Map<String, String> filters){
        LOG.fine("Attempt to query departments.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if(!getRole(decodedToken).equals(BO)) {
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
        }

        if( filters == null){
            filters = new HashMap<>(1);
        }

        StructuredQuery.CompositeFilter attributeFilter = null;
        StructuredQuery.PropertyFilter propFilter;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            propFilter = StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue());

            if(attributeFilter == null)
                attributeFilter = StructuredQuery.CompositeFilter.and(propFilter);
            else
                attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);
        }

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(DEPARTMENT)
                .setFilter(attributeFilter)
                .setLimit(Integer.parseInt(limit))
                .setOffset(Integer.parseInt(offset))
                .build();
        QueryResults<Entity> queryResults = datastore.run(query);
        List<Entity> results = new ArrayList<>();
        queryResults.forEachRemaining(results::add);

        LOG.info("Ides receber um query ó filho!");
        Gson g = new Gson();
        return Response.ok(g.toJson(results)).build();

    }

    @POST
    @Path("/add/members/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addMembers(@HeaderParam("Authorization") String token,
                               @PathParam("id") String id,
                               DepartmentData data) {
        LOG.fine("Attempt to add members to the department.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(id);
            Entity department = txn.get(departmentKey);

            if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            }
            if( !getRole(decodedToken).equals(BO) && !department.getString("president").equals(decodedToken.getUid()) ) {
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }

            String list = department.getString("members_list");
            String userPersonalList;
            String[] attributes;
            Key memberKey;
            Entity memberEntity;
            Entity newUser;
            for(String valuesOfMember : data.members) {
                attributes = valuesOfMember.split("%");
                memberKey = datastore.newKeyFactory().setKind(USER).newKey(attributes[1]);
                memberEntity = txn.get(memberKey);

                if(memberEntity == null){
                    txn.rollback();
                    LOG.warning(WRONG_MEMBER);
                    return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_MEMBER).build();
                }

                if (!list.contains(attributes[1])) {
                    userPersonalList = memberEntity.getString("job_list");
                    userPersonalList = userPersonalList.concat("#" + department.getString("id") + "%" + attributes[0]);
                    newUser = Entity.newBuilder(memberEntity)
                            .set("job_list", userPersonalList)
                            .set("time_lastupdate", Timestamp.now())
                            .build();
                    txn.update(newUser);
                    list = list.concat("#" + valuesOfMember);
                }
            }
            Entity updatedDepartment = Entity.newBuilder(department)
                    .set("members_list", list)
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.update(updatedDepartment);
            txn.commit();

            LOG.info("Members added.");
            return Response.ok(updatedDepartment).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @PATCH
    @Path("/delete/members/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteMembers(@HeaderParam("Authorization") String token,
                                  @PathParam("id") String id, DepartmentData data) {
        LOG.fine("Attempt to remove members from the department.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if(data.validateList()){
            LOG.warning("List is empty.");
            return Response.status(Response.Status.BAD_REQUEST).entity("List is empty").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(id);
            Entity department = txn.get(departmentKey);

            if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            }
            if( !getRole(decodedToken).equals(BO) && !department.getString("president").equals(decodedToken.getUid()) ) {
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }

            String list = department.getString("members_list");
            String userPersonalList;
            String[] attributes;
            Key memberKey;
            Entity memberEntity;
            Entity newUser;
            for(String valuesOfMember : data.members) {
                attributes = valuesOfMember.split("%");
                memberKey = datastore.newKeyFactory().setKind(USER).newKey(attributes[1]);
                memberEntity = txn.get(memberKey);

                if(memberEntity == null){
                    txn.rollback();
                    LOG.warning(WRONG_MEMBER);
                    return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_MEMBER).build();
                }

                userPersonalList = memberEntity.getString("job_list");
                userPersonalList = userPersonalList.replace("#" + department.getString("id") + "%" + attributes[0], "");
                newUser = Entity.newBuilder(memberEntity)
                        .set("job_list", userPersonalList)
                        .set("time_lastupdate", Timestamp.now())
                        .build();
                txn.update(newUser);
                list = list.replace("#"+valuesOfMember, "");
            }
            Entity updatedDepartment = Entity.newBuilder(department)
                    .set("members_list", list)
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.update(updatedDepartment);
            txn.commit();

            LOG.info("Members removed.");
            return Response.ok(updatedDepartment).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @PATCH
    @Path("/edit/members/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editMembers(@HeaderParam("Authorization") String token,
                                @PathParam("id") String id, DepartmentData data) {
        LOG.fine("Attempt to edit members of the department.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if(data.validateList()){
            LOG.warning("List is empty.");
            return Response.status(Response.Status.BAD_REQUEST).entity("List is empty").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(id);
            Entity department = txn.get(departmentKey);

            if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            }
            if( !getRole(decodedToken).equals(BO) && !department.getString("president").equals(decodedToken.getUid()) ) {
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }

            String list = department.getString("members_list");
            String userPersonalList;
            String[] jobs;
            String targetJob;
            String[] attribute;
            Key memberKey;
            Entity memberEntity;
            Entity newUser;
            for(String valuesOfMember : data.members) {
                attribute = valuesOfMember.split("%");
                memberKey = datastore.newKeyFactory().setKind(USER).newKey(attribute[1]);
                memberEntity = txn.get(memberKey);

                if(memberEntity == null){
                    txn.rollback();
                    LOG.warning(WRONG_MEMBER);
                    return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_MEMBER).build();
                }

                userPersonalList = memberEntity.getString("job_list");
                jobs = userPersonalList.split("#");
                targetJob = null;
                for (String job: jobs) {
                    if (job.contains(department.getString("id"))) {
                        targetJob = job.split("%")[1];
                        break;
                    }
                }
                list = list.replace(targetJob +"%"+attribute[1], attribute[0]+"%"+attribute[1]);
                userPersonalList = userPersonalList.replace(department.getString("id") + "%" + targetJob, department.getString("id") + "%" + attribute[0]);
                newUser = Entity.newBuilder(memberEntity)
                        .set("job_list", userPersonalList)
                        .set("time_lastupdate", Timestamp.now())
                        .build();
                txn.update(newUser);
            }
            Entity updatedDepartment = Entity.newBuilder(department)
                    .set("members_list", list)
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.update(updatedDepartment);
            txn.commit();

            LOG.info("Members edited.");
            return Response.ok(updatedDepartment).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


}
