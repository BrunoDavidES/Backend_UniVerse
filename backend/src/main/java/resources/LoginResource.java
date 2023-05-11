package resources;

import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import util.UserData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
	public Response login(UserData data) {
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
