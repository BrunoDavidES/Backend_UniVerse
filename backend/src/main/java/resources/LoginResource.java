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


	private static final String BO = "BO";
	private static final String D = "D";
	private static final String ROLE = "role";
	private static final String USER = "User";
	private static final String EVENT = "Event";
	private static final String NEWS = "News";
	private static final String USER_CLAIM = "user";
	private static final String NAME_CLAIM = "name";
	private static final String MISSING_OR_WRONG_PARAMETER = "Missing or wrong parameter.";
	private static final String MISSING_PARAMETER = "Missing parameter.";
	private static final String TOKEN_NOT_FOUND = "Token not found.";
	private static final String USER_DOES_NOT_EXIST = "User does not exist.";
	private static final String USER_OR_PASSWORD_INCORRECT = "User or password incorrect.";
	private static final String PASSWORD_INCORRECT = "Password incorrect.";
	private static final String NICE_TRY = "Nice try but your not a capi person.";
	private static final String PERMISSION_DENIED = "Permission denied.";
	private static final String DEPARTMENT = "Department";
	private static final String WRONG_PRESIDENT = "President doesn't exists.";
	private static final String DEPARTMENT_ALREADY_EXISTS = "Department already exists.";
	private static final String WRONG_DEPARTMENT = "Department does not exist.";
	private static final String WRONG_MEMBER = "Member doesn't exists.";
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LoginResource() { }

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(@Context HttpServletRequest request, @Context HttpServletResponse response, UserData data) {
		LOG.fine("Attempt to login user: " + data.email);

		if(!data.validateLogin() ) {
			LOG.warning(MISSING_PARAMETER);
			return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_PARAMETER).build();
		}

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind(USER).addAncestor(PathElement.of(DEPARTMENT, "default")).newKey(data.username);
			Entity user = txn.get(userKey);

			if( user == null ) {
				txn.rollback();
				LOG.warning(USER_DOES_NOT_EXIST);
				return Response.status(Response.Status.UNAUTHORIZED).entity(USER_OR_PASSWORD_INCORRECT).build();
			} else {
				if(user.getString("password").equals(DigestUtils.sha512Hex(data.password))) {
					LOG.info("User logged in: " + data.username);

					loginToken(response, user.getString(NAME_CLAIM), data.username, user.getString(ROLE));

					txn.commit();
					return Response.ok(user).build();
				} else {
					txn.rollback();
					LOG.warning(PASSWORD_INCORRECT);
					return Response.status(Response.Status.UNAUTHORIZED).entity(USER_OR_PASSWORD_INCORRECT).build();
				}
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	@POST
	@Path("/backOffice")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loginBackOffice(@Context HttpServletRequest request, @Context HttpServletResponse response, UserData data){
		LOG.fine("Attempt to login user: " + data.email);

		if(!data.validateLogin()) {
			LOG.warning(MISSING_PARAMETER);
			return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_PARAMETER).build();
		}

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind(USER).newKey(data.username);
			Entity user = txn.get(userKey);

			if( user == null ) {
				txn.rollback();
				LOG.warning(USER_DOES_NOT_EXIST);
				return Response.status(Response.Status.UNAUTHORIZED).entity(USER_OR_PASSWORD_INCORRECT).build();
			} else {
				if(!user.getString(ROLE).equals(BO)){
					txn.rollback();
					LOG.warning(NICE_TRY);
					return Response.status(Response.Status.BAD_REQUEST).entity(CAPI).build();
				}
				if(user.getString("password").equals(DigestUtils.sha512Hex(data.password))) {
					LOG.info("User logged in: " + data.username);

					loginToken(response, user.getString(NAME_CLAIM), data.username, user.getString(ROLE));

					txn.commit();
					return Response.ok(user).build();
				} else {
					txn.rollback();
					LOG.warning(PASSWORD_INCORRECT);
					return Response.status(Response.Status.UNAUTHORIZED).entity(USER_OR_PASSWORD_INCORRECT).build();
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

			claims.put(USER_CLAIM, username);
			claims.put(NAME_CLAIM, name);
			claims.put(ROLE, role);

			String token = generator.generateToken(claims);
			Cookie cookie = new Cookie("token", token);
			cookie.setHttpOnly(true);
			cookie.setMaxAge(3600);
			cookie.setPath("/rest");
			cookie.setSecure(true); // Set the Secure attribute to ensure the cookie is only sent over HTTPS
			response.addCookie(cookie);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
