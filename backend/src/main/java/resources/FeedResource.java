package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseToken;
import util.FeedData;

import com.google.gson.Gson;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Logger;

import static util.FirebaseAuth.*;
import static util.Constants.*;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedResource {
    private static final Logger LOG = Logger.getLogger(FeedResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/post/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postEntry(@HeaderParam("Authorization") String token, @PathParam("kind") String kind, FeedData data){
        LOG.fine("Attempt to post entry to feed.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if((!kind.equals(NEWS) && !kind.equals(EVENT)) || !data.validate(kind)) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        try {
            String role = String.valueOf(getRole(decodedToken)).replaceAll("\"", "");
            String name = String.valueOf(decodedToken.getName()).replaceAll("\"", "");
            String username = String.valueOf(decodedToken.getUid()).replaceAll("\"", "");


            // Para já, só docentes é q fazem eventos
            // No futuro, pôr presidente da AE e possivelmente dos Nucleos
            if (kind.equals(EVENT) && !role.equals(D) && !role.equals(BO)){
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
                if (kind.equals(EVENT)) { //construtor de eventos

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
                    // Caso se vá buscar uma notícia de outro site, por parte do backoffice,
                    // e se queira por o author como "Jornal Expresso", por exemplo
                    if (role.equals(BO) && data.authorNameByBO != null && !data.authorNameByBO.equals("")){
                        name = data.authorNameByBO;
                    }
                    builder.set("id", id)
                            .set("title", data.title)
                            .set("authorName", name)
                            .set("authorUsername", username)
                            .set("validated_backoffice", "false")
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
    public Response editEntry(@HeaderParam("Authorization") String token, @PathParam("kind") String kind, @PathParam("id") String id, FeedData data){
        LOG.fine("Attempt to edit feed entry.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if(!kind.equals(NEWS) && !kind.equals(EVENT)) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            String role = String.valueOf(getRole(decodedToken)).replaceAll("\"", "");
            String username = String.valueOf(decodedToken.getUid()).replaceAll("\"", "");

            if( entry == null ) {
                txn.rollback();
                LOG.warning(kind + " does not exist " + id);
                return Response.status(Response.Status.BAD_REQUEST).entity(kind + " does not exist " + id).build();
            } else if (!data.validateEdit(entry, kind)) {
                txn.rollback();
                LOG.warning("Invalid request for editEntry");
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request").build();
            }

            if( !(entry.getString("authorUsername").equals(username) || role.equals(BO)) ){
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
            }else {

                Entity.Builder newEntry = Entity.newBuilder(entry);
                if (kind.equals(EVENT)) { //construtor de eventos
                    newEntry.set("title", data.title)
                            .set("startDate", data.startDate)
                            .set("endDate", data.endDate)
                            .set("location", data.location)
                            .set("department", data.department)
                            .set("isPublic", data.isPublic)
                            .set("capacity", data.capacity)
                            .set("isItPaid", data.isItPaid)
                            .set("validated_backoffice", data.validated_backoffice)
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
    public Response deleteEntry(@HeaderParam("Authorization") String token, @PathParam("kind") String kind, @PathParam("id") String id){
        LOG.fine("Attempt to delete event.");

        FirebaseToken decodedToken = authenticateToken(token);
        if(decodedToken == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build();
        }

        if((!kind.equals(NEWS) && !kind.equals(EVENT))) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key eventKey = datastore.newKeyFactory().setKind(kind).newKey(id);
            Entity entry = txn.get(eventKey);

            String role = String.valueOf(getRole(decodedToken)).replaceAll("\"", "");
            String username = String.valueOf(decodedToken.getUid()).replaceAll("\"", "");

            if( entry == null ) {
                txn.rollback();
                LOG.warning(kind + " does not exist");
                return Response.status(Response.Status.BAD_REQUEST).entity(kind + " does not exist").build();
            } else if( !(entry.getString("authorUsername").equals(username) || role.equals(BO)) ) {
                txn.rollback();
                LOG.warning(PERMISSION_DENIED);
                return Response.status(Response.Status.FORBIDDEN).entity(PERMISSION_DENIED).build();
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
    public Response queryEntries(@HeaderParam("Authorization") String token, @PathParam("kind") String kind,
                                @QueryParam("limit") String limit,
                                @QueryParam("offset") String offset, Map<String, String> filters){
        LOG.fine("Attempt to query feed " + kind);

        //Verificar, caso for evento privado, se o token é valido
        if(kind.equals(EVENT)) {
            FirebaseToken decodedToken = authenticateToken(token);

            if (decodedToken == null) {
                LOG.info(TOKEN_NOT_FOUND);
                if (filters == null)
                    filters = new HashMap<>(1);
                filters.put("isPublic", "yes");
            }
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

        Query<Entity> query = Query.newEntityQueryBuilder() //tá feio mas só funciona assim, raios da datastore
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

    @POST
    @Path("/numberOf/{kind}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response queryEntriesNum(@HeaderParam("Authorization") String token, @PathParam("kind") String kind, Map<String, String> filters) {
        LOG.fine("Attempt to count the query feed " + kind);

        // Verificar, caso for evento privado, se o token é valido
        if (kind.equals(EVENT)) {
            FirebaseToken decodedToken = authenticateToken(token);

            if (decodedToken == null) {
                LOG.info(TOKEN_NOT_FOUND);
                if (filters == null)
                    filters = new HashMap<>(1);
                filters.put("isPublic", "yes");
            }
        }

        QueryResults<Entity> queryResults;

        StructuredQuery.CompositeFilter attributeFilter = null;
        if (filters == null) {
            filters = new HashMap<>(1);
        }
        StructuredQuery.PropertyFilter propFilter;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            propFilter = StructuredQuery.PropertyFilter.eq(entry.getKey(), entry.getValue());

            if (attributeFilter == null)
                attributeFilter = StructuredQuery.CompositeFilter.and(propFilter);
            else
                attributeFilter = StructuredQuery.CompositeFilter.and(attributeFilter, propFilter);
        }

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(attributeFilter)
                .build();

        queryResults = datastore.run(query);

        List<Entity> results = new ArrayList<>();

        //queryResults.forEachRemaining(results::add);

        LOG.info("Received a query!");
        int count = 0;
        // Get the total number of entities
        while (queryResults.hasNext()) {
            results.add(queryResults.next());
            count++;
        }
        // Convert the response object to JSON
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(count);

        return Response.ok(jsonResponse).build();
    }
}
