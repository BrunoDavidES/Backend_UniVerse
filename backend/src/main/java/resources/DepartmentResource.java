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

import static utils.Constants.*;
import static utils.FirebaseAuth.authenticateToken;
import static utils.FirebaseAuth.getRole;

@Path("/department")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DepartmentResource {
    private static final Logger LOG = Logger.getLogger(DepartmentResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerDepartment(@HeaderParam("Authorization") String token, DepartmentData data) {
        LOG.fine("Attempt to register department: " + data.id);

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        if( !data.validateRegister() ) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            String role = getRole(decodedToken);
            if(!role.equals(BO) && !role.equals(ADMIN)){
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }

            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.id);
            Entity department = txn.get(departmentKey);

            Key presidentKey = datastore.newKeyFactory().setKind(USER).newKey(data.president);
            Entity president = txn.get(presidentKey);

            if( president == null ) {
                txn.rollback();
                LOG.warning(WRONG_PRESIDENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PRESIDENT).build();
            } else if( department != null ) {
                txn.rollback();
                LOG.warning(DEPARTMENT_ALREADY_EXISTS);
                return Response.status(Response.Status.BAD_REQUEST).entity(DEPARTMENT_ALREADY_EXISTS).build();
            } else {
                department = Entity.newBuilder(departmentKey)
                        .set("id", data.id)
                        .set("email", data.email)
                        .set("name", data.name)
                        .set("president", data.president)
                        .set("phone_number", data.phoneNumber)
                        .set("location", data.location)
                        .set("fax", data.fax)
                        .set("time_creation", Timestamp.now())
                        .set("time_lastupdate", Timestamp.now())
                        .build();
                txn.add(department);

                Entity updatedProfessor = Entity.newBuilder(president)
                        .set("department", data.id)
                        .set("department_job", "President")
                        .build();
                txn.update(updatedProfessor);

                LOG.info("Department registered " + data.id);
                txn.commit();
                return Response.ok(department).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


    @POST
    @Path("/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyDepartment(@HeaderParam("Authorization") String token, DepartmentData data){
        LOG.fine("Attempt to modify department.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        if( !data.validateModify()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(data.id);
            Entity department = txn.get(departmentKey);

            if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            }

            data.fillGaps(department);
            Key presidentKey = datastore.newKeyFactory().setKind(USER).newKey(data.president);
            Entity president = txn.get(presidentKey);

            String role = getRole(decodedToken);
            if(!role.equals(BO) && !role.equals(ADMIN)){
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }else if( president == null ) {
                txn.rollback();
                LOG.warning(WRONG_PRESIDENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PRESIDENT).build();
            }else {
                String prevPresident = department.getString("president");
                if ( !data.president.equals( prevPresident )){
                    Key previousPresidentKey = datastore.newKeyFactory().setKind(USER).newKey(prevPresident);

                    Entity newPreviousPresident = Entity.newBuilder(txn.get(previousPresidentKey))
                            .set("department_job", "Docente")
                            .build();
                    txn.update(newPreviousPresident);

                    Entity newPresident = Entity.newBuilder(presidentKey)
                            .set("department", data.id)
                            .set("department_job", "Presidente")
                            .build();
                    txn.update(newPresident);
                }

                Entity updatedDepartment = Entity.newBuilder(department)
                        .set("email", data.email)
                        .set("name", data.name)
                        .set("president", data.president)
                        .set("phone_number", data.phoneNumber)
                        .set("location", data.location)
                        .set("fax", data.fax)
                        .set("time_lastupdate", Timestamp.now())
                        .build();

                txn.update(updatedDepartment);
                LOG.info(data.id + " edited.");
                txn.commit();
                return Response.ok(updatedDepartment).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/delete/{id}")
    public Response deleteDepartment(@HeaderParam("Authorization") String token, @PathParam("id") String id){
        LOG.fine("Attempt to delete department.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(id);
            Entity department = txn.get(departmentKey);

            String role = getRole(decodedToken);
            if(!role.equals(BO) && !role.equals(ADMIN)){  //SE CALHAR PODE SE POR ROLE MINIMO COMO PROFESSOR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }else if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            } else {
                Query<Entity> query = Query.newEntityQueryBuilder()
                        .setKind("User")
                        .setFilter(StructuredQuery.PropertyFilter.eq("department", id))
                        .build();

                QueryResults<Entity> queryResults = datastore.run(query);
                while (queryResults.hasNext()) {
                    Entity userEntity = queryResults.next();
                    userEntity = Entity.newBuilder(userEntity)
                            .set("department", "")
                            .set("department_job", "")
                            .build();
                    txn.update(userEntity);
                }
                txn.delete(departmentKey);
                LOG.info("Department deleted.");
                txn.commit();
                return Response.ok(department).build();
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
    public Response queryDepartment(@HeaderParam("Authorization") String token,
                                    @QueryParam("limit") String limit,
                                    @QueryParam("offset") String cursor, Map<String, String> filters){
        LOG.fine("Attempt to query departments.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }

        QueryResults<Entity> queryResults;

        StructuredQuery.CompositeFilter attributeFilter = null;
        if( filters == null ){
            filters = new HashMap<>(1);
        }
        StructuredQuery.PropertyFilter propFilter;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            propFilter = StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue());

            if(attributeFilter == null)
                attributeFilter = StructuredQuery.CompositeFilter.and(propFilter);
            else
                attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);
        }

        EntityQuery.Builder query = Query.newEntityQueryBuilder()
                .setKind(DEPARTMENT)
                .setFilter(attributeFilter)
                .setLimit(Integer.parseInt(limit));

        if ( !cursor.equals("EMPTY") ){
            query.setStartCursor(Cursor.fromUrlSafe(cursor));
        }

        queryResults = datastore.run(query.build());

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        LOG.info("Query de departamentos pedido");
        Gson g = new Gson();

        return Response.ok(g.toJson(results))
                .header("X-Cursor",queryResults.getCursorAfter().toUrlSafe())
                .build();
    }
}