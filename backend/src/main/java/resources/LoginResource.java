package resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
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

	private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

	public LoginResource() { }

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(@Context HttpServletRequest request, @Context HttpServletResponse response, UserData data) {
		LOG.fine("Attempt to login user: ");

		try {
			FirebaseToken decodedToken = firebaseAuth.verifyIdToken(data.token);
			String uid = decodedToken.getUid();

			UserRecord userRecord = firebaseAuth.getUser(decodedToken.getUid());

			loginToken(response, userRecord.getDisplayName(), userRecord.getUid(), userRecord.getCustomClaims().get("role").toString());

			LOG.info("User logged in: " + uid);
			return Response.ok(uid).build();
		} catch (FirebaseAuthException e) {
			LOG.warning("User login failed: " + e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity("User login failed: " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response validateLogin(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		LOG.fine("Attempt to validate login: ");

		DecodedJWT token = AuthToken.validateToken(request);

		if (token == null) {
			LOG.warning("Token not found");
			return Response.status(Response.Status.FORBIDDEN).entity("Token invalid").build();
		}

		return Response.ok(token).build();
	}

	private void loginToken(HttpServletResponse response, String name, String username, String role) {
		try {
			AuthToken generator = new AuthToken();
			Map<String, String> claims = new HashMap<>();

			claims.put("user", username);
			claims.put("name", name);
			claims.put("role", role);

			String token = generator.generateToken(claims);
			String cookie = "token=" + token +
					"; Path=/rest" +
					"; Secure" +
					"; HttpOnly" +
					"; SameSite=None";  // Set the SameSite attribute to "Strict"

			response.setHeader("Set-Cookie", cookie);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}


/*package resources;

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

					loginToken(response, user.getString("name"), data.username, user.getString("role"));

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

	@POST
	@Path("/backOffice")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loginBackOffice(@Context HttpServletRequest request, @Context HttpServletResponse response, UserData data) {
		LOG.fine("Attempt to login user: " + data.email);

		if(!data.validateLogin()) {
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
				if(!user.getString("role").equals("BO")){
					txn.rollback();
					LOG.warning("Nice try but your not a capi person.");
					return Response.status(Response.Status.BAD_REQUEST).entity("Your not one of us\n" +
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
							"⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠙⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀").build();
				}
				if(user.getString("password").equals(DigestUtils.sha512Hex(data.password))) {
					LOG.info("User logged in: " + data.username);

					loginToken(response, user.getString("name"), data.username, user.getString("role"));

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
			cookie.setMaxAge(3600);
			cookie.setPath("/rest");
			cookie.setSecure(true); // Set the Secure attribute to ensure the cookie is only sent over HTTPS
			response.addCookie(cookie);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}*/
