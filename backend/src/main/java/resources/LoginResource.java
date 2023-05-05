package resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;
import util.AuthToken;
import util.LoginData;


@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final String USER = "USER";

	public Gson g = new Gson();

	private static final Logger Log = Logger.getLogger(LoginResource.class.getName());

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	public LoginResource() {	}


	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLogin(LoginData data) {

		Log.info("Attempt to login user: " + data.username);

		Key userKey = userKeyFactory.newKey(data.username);

		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.username);

		Transaction txn = datastore.newTransaction();

		try{
			Entity user = txn.get(userKey);

			if (user == null) {
				txn.rollback();
				Log.warning("Username " + data.username + " does not exists.");
				return Response.status(Status.FORBIDDEN).entity("Username " + data.username + " does not exists.").build();
			}

			String hashedPassword = (String) user.getString("password");

			if (data.isPasswordValid(hashedPassword)) {
				AuthToken auToken = new AuthToken(data.username);
				Entity token = txn.get(tokenKey);

				if (token != null  &&  token.getLong("expirationDate") >= System.currentTimeMillis()  ) {
					txn.rollback();
					Log.warning("User " + data.username + "  is already logged in.");
					return Response.status(Status.FORBIDDEN).entity("User " + data.username + "  is already logged in.").build();
				}

				token = Entity.newBuilder(tokenKey)
						.set("username", auToken.username)
						.set("tokenId", auToken.tokenId)
						.set("creationDate", auToken.creationDate)
						.set("expirationDate", auToken.expirationDate)
						.build();

				auToken.setRole(user.getString("role"));

				txn.put(token);
				txn.commit();

				Log.info("User " + data.username + "' logged in successfully.");
				return Response.ok(g.toJson(auToken)).build();
			}
			else {
				txn.rollback();
				Log.warning("Wrong password for username " + data.username + ".");
				return Response.status(Status.FORBIDDEN).entity("Wrong password for username " + data.username + ".").build();
			}
		}  finally{
			if (txn.isActive()) {
				txn.rollback();
				//return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error 500. Something went wrong with your request.").build();
			}
		}
	}

}
