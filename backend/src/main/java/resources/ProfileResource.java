package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import util.ProfileData;
import util.ValToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/profile")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ProfileResource {
    private static final Logger LOG = Logger.getLogger(ProfileResource.class.getName());

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public Gson g = new Gson();

    // Talvez adicionar LinkedIn

    @GET
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getProfile(@Context HttpServletRequest request, @PathParam("username") String username){
        LOG.fine("Attempt to get profile by " + username);

        if(username == null){
            LOG.warning("username field is null");
            return Response.status(Response.Status.BAD_REQUEST).entity("Empty param").build();
        }



        final ValToken validator = new ValToken();
        DecodedJWT token = validator.checkToken(request);

        if (token == null) {
            LOG.warning("Token not found");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Token not found").build();
        }
        //String.valueOf(token.getClaim("user")).replaceAll("\"", "")
        String requester = String.valueOf(token.getClaim("user")).replaceAll("\"", "");
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(requester);
        Entity user = datastore.get(userKey);

        if( user == null ) {
            LOG.warning("User does not exist");
            return Response.status(Response.Status.BAD_REQUEST).entity("User does not exist "  + requester).build();
        }

        // Vai ter de mudar quando se souber os atributos a devolver em cada caso
        if (!username.equals(requester)){
            // Faz um perfil menos completo
            userKey = datastore.newKeyFactory().setKind("User").newKey(username);
            user = datastore.get(userKey);

            if( user == null ) {
                LOG.warning("User or password incorrect");
                return Response.status(Response.Status.BAD_REQUEST).entity("User or password incorrect").build();
            }
        }
        // Enquanto não virmos quais os atributos a devolver em cada caso, vamos dar poucos
        ProfileData data = new ProfileData();
        // Enquanto não virmos quais os atributos a devolver em cada caso, vamos dar poucos
        data.username = username;
        data.name = user.getString("name");
        data.role = user.getString("role");
        data.jobs = user.getString("job_list");

        LOG.fine("Profile successfully gotten");
        return Response.ok(g.toJson(data)).build();
    }



}

