package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import util.FeedData;

import com.google.gson.Gson;
import util.ValToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Logger;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedResource {
    private static final Logger LOG = Logger.getLogger(FeedResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    @POST
    @Path("/post/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postEntry(@Context HttpServletRequest request, @PathParam("kind") String kind, FeedData data){
        LOG.fine("Attempt to post entry to feed.");

        if((!kind.equals("News") && !kind.equals("Event")) || !data.validate(kind)) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.FORBIDDEN).entity("Token not found").build();
            }

            String role = String.valueOf(token.getClaim("role")).replaceAll("\"", "");
            String name = String.valueOf(token.getClaim("name")).replaceAll("\"", "");
            String username = String.valueOf(token.getClaim("user")).replaceAll("\"", "");


            // Para já, só docentes é q fazem eventos
            // No futuro, pôr presidente da AE e possivelmente dos Nucleos
            if (kind.equals("Event") && !role.equals("D") && !role.equals("BO")){
                LOG.warning("No permission to create an event.");
                return Response.status(Response.Status.FORBIDDEN).entity("No permission to create an event.").build();
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
                } while (entry != null);

                Entity.Builder builder = Entity.newBuilder(feedKey);
                if (kind.equals("Event")) { //construtor de eventos

                    builder.set("id", id)
                            .set("title", data.title)
                            .set("authorName", name)
                            .set("authorUsername", username)
                            .set("startDate", data.startDate)
                            .set("endDate", data.endDate)
                            .set("location", data.location)
                            .set("department", data.department)
                            .set("isPublic", data.isPublic)
                            .set("capacity", data.capacity)
                            .set("isItPaid", data.isItPaid)
                            .set("validated_backoffice", "false")
                            .set("time_creation", Timestamp.now());

                }else { //construtor de news

                    builder.set("id", id)
                            .set("title", data.title)
                            .set("authorName", name)
                            .set("authorUsername", username)
                            .set("validated_backoffice", "true")
                            .set("time_creation", Timestamp.now());

                }
                entry = builder.build();

                txn.add(entry);
                LOG.info(kind + " posted " + data.title + "; id: " + id);
                txn.commit();
                return Response.ok(id).build();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        } catch (InvalidParameterException e) {
            LOG.warning("Token is invalid: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Token is invalid").build();
        }
    }

    @PATCH
    @Path("/edit/{kind}/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editEntry(@Context HttpServletRequest request, @PathParam("kind") String kind, @PathParam("id") String id, FeedData data){
        LOG.fine("Attempt to edit feed entry.");

        if(!kind.equals("News") && !kind.equals("Event")) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.FORBIDDEN).entity("Token not found").build();
            }


            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            String role = String.valueOf(token.getClaim("role")).replaceAll("\"", "");
            String username = String.valueOf(token.getClaim("user")).replaceAll("\"", "");

            if( entry == null ) {
                txn.rollback();
                LOG.warning(kind + " does not exist " + id);
                return Response.status(Response.Status.BAD_REQUEST).entity(kind + " does not exist " + id).build();
            } else if (!data.validateEdit(entry, kind)) {
                txn.rollback();
                LOG.warning("Invalid request for editEntry");
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request").build();
            }

            if( !(entry.getString("authorUsername").equals(username) || role.equals("BO")) ){
                txn.rollback();
                LOG.warning("Permission denied");
                return Response.status(Response.Status.FORBIDDEN).entity("Permission denied").build();
            }else {

                Entity.Builder newEntry = Entity.newBuilder(entry);
                if (kind.equals("Event")) { //construtor de eventos
                    newEntry.set("title", data.title)
                            .set("startDate", data.startDate)
                            .set("endDate", data.endDate)
                            .set("location", data.location)
                            .set("department", data.department)
                            .set("isPublic", data.isPublic)
                            .set("capacity", data.capacity)
                            .set("isItPaid", data.isItPaid)
                            .set("time_lastupdated", Timestamp.now());
                }else { //construtor de news
                    newEntry.set("title", data.title)
                            .set("time_lastupdated", Timestamp.now());

                }
                Entity updatedEntryEntry = newEntry.build();
                txn.update(updatedEntryEntry);

                LOG.info(kind + " edited " + data.title + "; id: " + id);
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
    @Path("/delete/{kind}/{id}")
    public Response deleteEntry(@Context HttpServletRequest request, @PathParam("kind") String kind, @PathParam("id") String id){
        LOG.fine("Attempt to delete event.");

        if((!kind.equals("News") && !kind.equals("Event"))) {
            LOG.warning("Missing or wrong parameter");
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
                return Response.status(Response.Status.FORBIDDEN).entity("Token not found").build();
            }

            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            String role = String.valueOf(token.getClaim("role")).replaceAll("\"", "");
            String username = String.valueOf(token.getClaim("user")).replaceAll("\"", "");

            if( entry == null ) {
                txn.rollback();
                LOG.warning(kind + " does not exist");
                return Response.status(Response.Status.BAD_REQUEST).entity(kind + " does not exist").build();
            } else if( !(entry.getString("authorUsername").equals(username) || role.equals("BO")) ) {
                txn.rollback();
                LOG.warning("Permission denied");
                return Response.status(Response.Status.FORBIDDEN).entity("Permission denied").build();
            }else {
                txn.delete(eventKey);
                LOG.info(kind + " deleted " + id);
                txn.commit();
                return Response.ok(entry).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/query/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryEntries(@Context HttpServletRequest request, @PathParam("kind") String kind,
                                @QueryParam("limit") String limit,
                                @QueryParam("offset") String offset, Map<String, String> filters){
        LOG.fine("Attempt to query feed " + kind);

        //Verificar, caso for evento privado, se o token é valido
        if(kind.equals("Event")) {
            final ValToken validator = new ValToken();
            DecodedJWT token = validator.checkToken(request);

            if (token == null) {
                LOG.warning("Token not found");
            filters.put("isPublic","yes");
            }
        }


        QueryResults<Entity> queryResults;

        StructuredQuery.CompositeFilter attributeFilter = null;
        if( filters == null){
            filters = new HashMap<>(1);
        }
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            StructuredQuery.PropertyFilter propFilter = StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue());

            if(attributeFilter == null)
                attributeFilter = StructuredQuery.CompositeFilter.and(propFilter);
            else
                attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);
        }

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(kind)
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


}
