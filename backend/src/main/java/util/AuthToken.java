package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import resources.LoginResource;

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

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private KeyPairGenerator keyPairGenerator;
	private KeyPair keyPair;

	private static final Logger LOG = Logger.getLogger(AuthToken.class.getName());

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
		

		LOG.info("Public Key: " + keyPair.getPublic() + " / Private Key: " + keyPair.getPrivate());
		return tokenBuilder.sign(Algorithm.RSA256(((RSAPublicKey) keyPair.getPublic()), (RSAPrivateKey) keyPair.getPrivate()));
	}
}