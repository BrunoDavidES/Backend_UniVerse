package resources;

import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import util.AuthToken;
import util.UserData;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LoginResource() { }

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(@Context HttpServletRequest request, @Context HttpServletResponse response, UserData data) {
		LOG.fine("Attempt to login user: " + data.email);

		if(!data.validateLogin() ) {
			LOG.warning("Missing parameter");
			return Response.status(Response.Status.BAD_REQUEST).entity("Missing parameter").build();
		}

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);

			if( user == null ) {
				txn.rollback();
				LOG.warning("User does not exist.");
				return Response.status(Response.Status.UNAUTHORIZED).entity("User or password incorrect").build();
			} else {
				if(user.getString("password").equals(DigestUtils.sha512Hex(data.password))) {
					LOG.info("User logged in: " + data.username);

					loginToken(response, data.username, data.name, user.getString("role"));

					txn.commit();
					return Response.ok(user).build();
				} else {
					txn.rollback();
					LOG.warning("Password incorrect.");
					return Response.status(Response.Status.UNAUTHORIZED).entity("User or password incorrect").build();
				}
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	private void loginToken(HttpServletResponse response, String name, String username, String role) {
		try {
			AuthToken generator = new AuthToken();
			Map<String, String> claims = new HashMap<>();

			claims.put("user", username);
			claims.put("name", name);
			claims.put("role", role);

			String token = generator.generateToken(claims);
			Cookie cookie = new Cookie("token", token);
			cookie.setHttpOnly(true);
			cookie.setMaxAge(600);
			response.addCookie(cookie);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
