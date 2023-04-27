package resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import util.AuthToken;
import util.UserData;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
	//private static final Datastore datastore = DatastoreOptions.newBuilder().setHost("localhost:8081").setProjectId("id").build().getService();

	public LoginResource() { }

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(UserData data, @Context HttpServletRequest request, @Context HttpServletResponse response) {
		LOG.fine("Attempt to login user: " + data.username);

		if(!data.validateLogin() ) {
			LOG.warning("Missing parameter");
			return Response.status(Response.Status.BAD_REQUEST).entity("Missing parameter.").build();
		}

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);

			if( user == null ) {
				txn.rollback();
				LOG.warning("User or password incorrect");
				return Response.status(Response.Status.BAD_REQUEST).entity("User or password incorrect").build();
			} else {
				if(user.getString("password").equals(DigestUtils.sha512Hex(data.password))) {
					try {
						AuthToken generator = new AuthToken();

						Map<String, String> claims = new HashMap<>();

						String status = user.getString("status");
						if(status.equals("INACTIVE")) {
							txn.rollback();
							LOG.warning("User is inactive");
							return Response.status(Response.Status.BAD_REQUEST).entity("User is inactive").build();
						}

						claims.put("user", data.username);
						claims.put("role", user.getString("role"));
						claims.put("status", status);

						String token = generator.generateToken(claims);
						Cookie cookie = new Cookie("token", token);
						cookie.setHttpOnly(true);
						cookie.setMaxAge(600);
						response.addCookie(cookie);
					} catch (Exception e) {
						e.printStackTrace();
					}
					LOG.info("User logged in: " + data.username);
					txn.commit();
					return Response.ok(user).build();
				} else {
					txn.rollback();
					LOG.warning("User or password incorrect");
					return Response.status(Response.Status.BAD_REQUEST).entity("User or password incorrect").build();
				}
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}


}
