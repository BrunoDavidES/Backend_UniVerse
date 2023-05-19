package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
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

		String publicKeyJwt = convertToJWK(publicKey);
		String privateKeyJwt = convertToJWK(privateKey);

		LOG.info("Public Key (JWT format):\n" + publicKeyJwt);
		LOG.info("Private Key (JWT format):\n" + privateKeyJwt);
		return tokenBuilder.sign(Algorithm.RSA256(((RSAPublicKey) keyPair.getPublic()), (RSAPrivateKey) keyPair.getPrivate()));
	}

	private String convertToJWK(RSAPublicKey publicKey) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec publicKeySpec = keyFactory.getKeySpec(publicKey, X509EncodedKeySpec.class);
		byte[] publicKeyBytes = publicKeySpec.getEncoded();
		String publicKeyBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKeyBytes);

		return "{\n" +
				"  \"kty\": \"RSA\",\n" +
				"  \"e\": \"" + Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray()) + "\",\n" +
				"  \"n\": \"" + publicKeyBase64 + "\"\n" +
				"}";
	}

	private String convertToJWK(RSAPrivateKey privateKey) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPrivateKeySpec rsaPrivateKeySpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);

		String modulus = Base64.getUrlEncoder().withoutPadding().encodeToString(rsaPrivateKeySpec.getModulus().toByteArray());
		String privateExponent = Base64.getUrlEncoder().withoutPadding().encodeToString(rsaPrivateKeySpec.getPrivateExponent().toByteArray());

		return "{\n" +
				"  \"kty\": \"RSA\",\n" +
				"  \"d\": \"" + privateExponent + "\",\n" +
				"  \"n\": \"" + modulus + "\"\n" +
				"}";
	}

}