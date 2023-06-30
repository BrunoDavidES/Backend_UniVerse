package resources;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.*;
import com.google.gson.Gson;
import util.DepartmentData;
import util.ValToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/department")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DepartmentResource {

    private static final String CAPI = "Your not one of us\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⢀⣞⣆⢀⣠⢶⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
            "⠀⢀⣀⡤⠤⠖⠒⠋⠉⣉⠉⠹⢫⠾⣄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
            "⢠⡏⢰⡴⠀⠀⠀⠉⠙⠟⠃⠀⠀⠀⠈⠙⠦⣄⡀⢀⣀⣠⡤⠤⠶⠒⠒⢿⠋⠈⠀⣒⡒⠲⠤⣄⡀⠀⠀⠀⠀⠀⠀\n" +
            "⢸⠀⢸⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠀⠴⠂⣀⠀⠀⣴⡄⠉⢷⡄⠚⠀⢤⣒⠦⠉⠳⣄⡀⠀⠀⠀\n" +
            "⠸⡄⠼⠦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣄⡂⠠⣀⠐⠍⠂⠙⣆⠀⠀\n" +
            "⠀⠙⠦⢄⣀⣀⣀⣀⡀⠀⢷⠀⢦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠰⡇⠠⣀⠱⠘⣧⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠈⠉⢷⣧⡄⢼⠀⢀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠀⡈⠀⢄⢸⡄\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⣿⡀⠃⠘⠂⠲⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⠀⡈⢘⡇\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢫⡑⠣⠰⠀⢁⢀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠁⣸⠁\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⣯⠂⡀⢨⠀⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡆⣾⡄⠀⠀⠀⠀⣀⠐⠁⡴⠁⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⣧⡈⡀⢠⣧⣤⣀⣀⡀⢀⡀⠀⠀⢀⣼⣀⠉⡟⠀⢀⡀⠘⢓⣤⡞⠁⠀⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢺⡁⢁⣸⡏⠀⠀⠀⠀⠁⠀⠉⠉⠁⠹⡟⢢⢱⠀⢸⣷⠶⠻⡇⠀⠀⠀⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢈⡏⠈⡟⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠑⢄⠁⠀⠻⣧⠀⠀⣹⠁⠀⠀⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣀⡤⠚⠃⣰⣥⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣾⠼⢙⡷⡻⠀⡼⠁⠀⠀⠀⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠟⠿⡿⠕⠊⠉⠀⠀⠀⠀⠀⠀⠀⠀⣠⣴⣶⣾⠉⣹⣷⣟⣚⣁⡼⠁⠀⠀⠀⠀⠀\n" +
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠙⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";

    private static final String MISSING_OR_WRONG_PARAMETER = "Missing or wrong parameter";
    private static final String TOKEN_NOT_FOUND = "Token not found";
    private static final String BO = "BO";
    private static final String ROLE = "role";
    private static final String USER = "User";
    private static final String USER_CLAIM = "user";
    private static final String NICE_TRY = "Nice try but your not a capi person";
    private static final String DEPARTMENT = "Department";
    private static final String WRONG_PRESIDENT = "President doesn't exists.";
    private static final String DEPARTMENT_ALREADY_EXISTS = "Department already exists.";
    private static final String WRONG_DEPARTMENT = "Department does not exist.";
    private static final String WRONG_MEMBER = "Member doesn't exists.";
    private static final Logger LOG = Logger.getLogger(DepartmentResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerDepartment(@Context HttpServletRequest request, DepartmentData data) {
        LOG.fine("Attempt to register department: " + data.id);

        if( !data.validateRegister() ) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }
            if(!String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "").equals(BO)){
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
                        .set("members_list", "")
                        .set("time_creation", Timestamp.now())
                        .set("time_lastupdate", Timestamp.now())
                        .build();
                txn.add(department);

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
    public Response modifyDepartment(@Context HttpServletRequest request, DepartmentData data){
        LOG.fine("Attempt to modify department.");

        if( !data.validateModify()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }
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
            if(!String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "").equals(BO)){
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }else if( president == null ) {
                txn.rollback();
                LOG.warning(WRONG_PRESIDENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_PRESIDENT).build();
            }else {

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

    @Path("/delete/{id}")
    @DELETE
    public Response deleteDepartment(@Context HttpServletRequest request, @PathParam("id") String id) {
        LOG.fine("Attempt to delete department.");

        Transaction txn = datastore.newTransaction();

        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(id);
            Entity department = txn.get(departmentKey);

            if (!String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "").equals(BO)) {
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            } else if (department == null) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            } else {

                // Update department's children
                Query<Entity> query = Query.newEntityQueryBuilder()
                        .setKind("User")
                        .setFilter(PropertyFilter.hasAncestor(departmentKey))
                        .build();

                QueryResults<Entity> queryResults = datastore.run(query);
                while (queryResults.hasNext()) {
                    Entity userEntity = queryResults.next();
                    userEntity = Entity.newBuilder(userEntity)
                            .set("department_job", "")
                            .build();
                    txn.update(userEntity);
                }
                // Delete department
                txn.delete(departmentKey);

                txn.commit();
                LOG.info("Department deleted.");
                return Response.ok(department).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
        //POR CURSOR EM VEZ DE OFFSET LIMIT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryDepartment(@Context HttpServletRequest request,
                               @QueryParam("limit") String limit,
                               @QueryParam("offset") String offset, Map<String, String> filters){
        LOG.fine("Attempt to query departments.");

        //Verificar, caso for evento privado, se o token é valido
        final ValToken validator = new ValToken();
        DecodedJWT token = validator.checkToken(request);

        if (token == null) {
            LOG.warning(TOKEN_NOT_FOUND);
            return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
        }
        if(!String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "").equals(BO)){
            LOG.warning(NICE_TRY);
            return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
        }
        QueryResults<Entity> queryResults;

        StructuredQuery.CompositeFilter attributeFilter = null;
        if( filters == null){
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

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(DEPARTMENT)
                .setFilter(attributeFilter)
                .setLimit(Integer.parseInt(limit))
                .setOffset(Integer.parseInt(offset))
                .build();

        queryResults = datastore.run(query);

        List<Entity> results = new ArrayList<>();

        queryResults.forEachRemaining(results::add);

        LOG.info("Ides receber um query ó filho!");
        Gson g = new Gson();
        return Response.ok(g.toJson(results)).build();

    }

    @POST
    @Path("/add/members/{id}")
    @Consumes(MediaType.APPLICATION_JSON)                                        //list composta por string que tem valor: "papel%username"
    public Response addMembers(@Context HttpServletRequest request, @PathParam("id") String id, DepartmentData data) {
        LOG.fine("Attempt to add members to the department.");

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(id);
            Entity department = txn.get(departmentKey);
            if(!String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "").equals(BO) && !department.getString("president").equals(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""))){  //SE CALHAR PODE SE POR ROLE MINIMO COMO PROFESSOR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }else if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            }
            String userJob;
            String[] attributes;
            Key memberKey;
            Entity memberEntity;
            Entity newUser;
            for(String valuesOfMember : data.members) {
                attributes = valuesOfMember.split("%");

                memberKey = datastore.newKeyFactory().setKind(USER).newKey(attributes[1]);
                Key updatedMemberKey = datastore.newKeyFactory().addAncestor(PathElement.of(DEPARTMENT, id)).setKind(USER).newKey(memberKey.getId());
                memberEntity = txn.get(updatedMemberKey);
                if (memberEntity == null) {
                    txn.rollback();
                    LOG.warning(WRONG_MEMBER);
                    return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_MEMBER).build();
                }
                userJob = memberEntity.getString("department_job");
                if (userJob.equals("")) {
                    newUser = Entity.newBuilder(memberEntity)
                            .setKey(updatedMemberKey)  // Set the key path as the key
                            .set("department_job", id + "%" + attributes[0])
                            .set("time_lastupdate", Timestamp.now())
                            .build();

                    txn.update(newUser);
                }
            }

            LOG.info("Members added.");
            txn.commit();
            return Response.ok(department).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


    @PATCH
    @Path("/delete/members/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteMembers(@Context HttpServletRequest request, @PathParam("id") String id, DepartmentData data) {
        LOG.fine("Attempt to remove members from the department.");

        if(data.validateList()){
            LOG.warning("List is empty.");
            return Response.status(Response.Status.BAD_REQUEST).entity("List is empty").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(id);
            Entity department = txn.get(departmentKey);
            if(!String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "").equals(BO) && !department.getString("president").equals(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""))){  //SE CALHAR PODE SE POR ROLE MINIMO COMO PROFESSOR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }else if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            }

            Query<Entity> query = Query.newEntityQueryBuilder()
                    .setKind("User")
                    .setFilter(PropertyFilter.hasAncestor(departmentKey))
                    .build();

            QueryResults<Entity> queryResults = datastore.run(query);
            while (queryResults.hasNext()) {
                Entity userEntity = queryResults.next();
                if(data.members.contains(userEntity.getString("department_job"))) {
                    userEntity = Entity.newBuilder(userEntity)
                            .set("department_job", "")
                            .build();
                    txn.update(userEntity);
                }
            }
            LOG.info("Members removed.");
            txn.commit();
            return Response.ok(department).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @PATCH
    @Path("/edit/members/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editMembers(@Context HttpServletRequest request, @PathParam("id") String id, DepartmentData data) {
        LOG.fine("Attempt to edit members of the department.");
        if(data.validateList()){
            LOG.warning("List is empty.");
            return Response.status(Response.Status.BAD_REQUEST).entity("List is empty").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning(TOKEN_NOT_FOUND);
                return Response.status(Response.Status.FORBIDDEN).entity(TOKEN_NOT_FOUND).build();
            }

            Key departmentKey = datastore.newKeyFactory().setKind(DEPARTMENT).newKey(id);
            Entity department = txn.get(departmentKey);
            if(!String.valueOf(token.getClaim(ROLE)).replaceAll("\"", "").equals(BO) && !department.getString("president").equals(String.valueOf(token.getClaim(USER_CLAIM)).replaceAll("\"", ""))){  //SE CALHAR PODE SE POR ROLE MINIMO COMO PROFESSOR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                txn.rollback();
                LOG.warning(NICE_TRY);
                return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
            }else if( department == null ) {
                txn.rollback();
                LOG.warning(WRONG_DEPARTMENT);
                return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_DEPARTMENT).build();
            }

            Query<Entity> query = Query.newEntityQueryBuilder()
                    .setKind("User")
                    .setFilter(PropertyFilter.hasAncestor(departmentKey))
                    .build();

            QueryResults<Entity> queryResults = datastore.run(query);
            while (queryResults.hasNext()) {
                Entity userEntity = queryResults.next();
                if(data.members.contains(userEntity.getString("department_job").split("%")[1])){
                    String job = "";
                    for (String j : data.members){
                        if(j.split("%")[1].equals(userEntity.getString("department_job").split("%")[1])){
                            job = j.split("%")[0];
                            break;
                        }
                    }
                    userEntity = Entity.newBuilder(userEntity)
                            .set("department_job", id + "%" + job)
                            .build();
                    txn.update(userEntity);
                }
            }
            LOG.info("Members edited.");
            txn.commit();
            return Response.ok(department).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}
