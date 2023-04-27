package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;


public class ValToken {

    private static final List<String> allowedIsses = Collections.singletonList("https://localhost:8080");

    public DecodedJWT validateToken(String token) {
        try {
            final DecodedJWT decoded = JWT.decode(token);

            if(!allowedIsses.contains(decoded.getIssuer())) {
                throw new InvalidParameterException(String.format("Unknown Issuer %s", decoded.getIssuer()));
            }

            Instant expiration = decoded.getExpiresAtAsInstant();
            if(expiration != null && expiration.isBefore(Instant.now())) {
                throw new TokenExpiredException("Token has expired.", expiration);
            }

            return decoded;
        } catch (Exception e) {
            throw new InvalidParameterException("Token validation failed: " + e.getMessage());
        }
    }


}
