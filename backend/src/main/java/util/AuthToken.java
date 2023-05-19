package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class AuthToken {

	private static final Logger LOG = Logger.getLogger(AuthToken.class.getName());

	private KeyPairGenerator keyPairGenerator;
	private KeyPair keyPair;

	public AuthToken () throws NoSuchAlgorithmException {
		keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		keyPair = keyPairGenerator.generateKeyPair();
	}

	public String generateToken(Map<String, String> payload) throws Exception {
		Builder tokenBuilder = JWT.create()
				.withIssuer("https://universe-fct.oa.r.appspot.com/")
				.withClaim("jti", UUID.randomUUID().toString())
				.withExpiresAt(Instant.now().plusSeconds(6000)) //2 fase implementar refresh
				.withIssuedAt(Instant.now());

		payload.entrySet().forEach(action -> tokenBuilder.withClaim(action.getKey(), action.getValue()));


		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		String token = tokenBuilder.sign(Algorithm.RSA256((publicKey), privateKey));

		if(verifyToken(token)) {
			LOG.info(publicKey.toString());
		}
		return token;
	}

	private boolean verifyToken(String token) throws JWTVerificationException {
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		try {
			JWTVerifier verifier = JWT.require(Algorithm.RSA256(publicKey)).build();
			DecodedJWT decodedJWT = verifier.verify(token);
			return true; // Verification successful
		} catch (JWTVerificationException e) {
			return false; // Verification failed
		}
	}


}