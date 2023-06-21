package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import services.ServerKeys;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthToken {
	private static final Logger LOG = Logger.getLogger(AuthToken.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final List<String> allowedIssuers = Collections.singletonList("https://universe-fct.oa.r.appspot.com/");

	public String generateToken(Map<String, String> payload) {
		Builder tokenBuilder = JWT.create()
				.withIssuer("https://universe-fct.oa.r.appspot.com/")
				.withClaim("jti", UUID.randomUUID().toString())
				.withIssuedAt(Instant.now())
				.withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS));

		payload.forEach(tokenBuilder::withClaim);

		return tokenBuilder.sign(Algorithm.HMAC512(ServerKeys.getRandomKey()));
	}

	public static DecodedJWT validateToken(HttpServletRequest request){
		String codedToken = null;

		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("token")) {
					codedToken = cookie.getValue();
					break;
				}
			}
		}

		return checkToken(codedToken);
	}

	public static void invalidateToken(HttpServletRequest request, HttpServletResponse response){
		String token = null;

		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("token")) {
					token = cookie.getValue();
					cookie.setValue(null);
					response.addCookie(cookie);
					break;
				}
			}
		}

		if(token != null) {
			DecodedJWT decoded = JWT.decode(token);
			Transaction txn = datastore.newTransaction();
			try {
				Key tokenKey = datastore.newKeyFactory().setKind("Token_Blacklist").newKey(decoded.getId());;

				Entity blToken = Entity.newBuilder(tokenKey)
						.set("expiration", Timestamp.of(decoded.getExpiresAt()))
						.build();
				txn.add(blToken);

				txn.commit();
			} finally {
				if (txn.isActive()) {
					txn.rollback();
				}
			}
		}
	}

	private static DecodedJWT checkToken(String token) {
		try{
			if (token == null)
				return null;

			DecodedJWT decoded = JWT.decode(token);
			boolean isTokenValid = false;

			String[] keys = ServerKeys.getServerKeys();
			for (String secretKey : keys) {
				try {
					Algorithm algorithm = Algorithm.HMAC512(secretKey);
					algorithm.verify(decoded);
					isTokenValid = true;
					break;
				} catch (Exception ignored) {}
			}

			if (!isTokenValid) {
				throw new InvalidParameterException("Token validation failed");
			}

			Transaction txn = datastore.newTransaction();
			try {
				Key tokenKey = datastore.newKeyFactory().setKind("Token_Blacklist").newKey(decoded.getId());
				Entity blToken = txn.get(tokenKey);

				if (blToken != null) {
					txn.rollback();
					LOG.warning("Token invalid.");
					throw new InvalidParameterException("Token validation failed");
				}
				txn.commit();

			} finally{
				if (txn.isActive()) {
					txn.rollback();
				}
			}

			if(!allowedIssuers.contains(decoded.getIssuer())) {
				throw new InvalidParameterException(String.format("Unknown Issuer %s", decoded.getIssuer()));
			}

			Instant expiration = decoded.getExpiresAtAsInstant();
			if(expiration != null && expiration.isBefore(Instant.now())) {
				throw new TokenExpiredException("Token has expired.", expiration);
			}

			return decoded;
		} catch (Exception e) {
			throw new InvalidParameterException("Token validation failed");
		}
	}


}