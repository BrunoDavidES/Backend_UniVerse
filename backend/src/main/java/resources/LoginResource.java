package resources;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import util.UserData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	public LoginResource() { }

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(UserData data) {
		LOG.fine("Attempt to login user: " + data.username);

		/*if (!data.validateLogin()) {
			LOG.warning("Missing or wrong parameter");
			return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter").build();
		}*/

		try {
			FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(data.token);
			String uid = token.getUid();

			LOG.info("User logged in: " + uid);
			return Response.ok(uid).build();
		} catch (FirebaseAuthException e) {
			LOG.warning("User login failed: " + e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity("User login failed: " + e.getMessage()).build();
		}
	}


}
