package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ValToken{

    private static final List<String> allowedIssuers = Collections.singletonList("https://universe-fct.oa.r.appspot.com/");

    public DecodedJWT validateToken(String token) {
        try {
            DecodedJWT decoded = JWT.decode(token);

            if(!allowedIssuers.contains(decoded.getIssuer())) {
                throw new InvalidParameterException(String.format("Unknown Issuer %s", decoded.getIssuer()));
            }

            Instant expiration = decoded.getExpiresAtAsInstant();
            if(expiration != null && expiration.isBefore(Instant.now())) {
                throw new TokenExpiredException("Token has expired.", expiration);
            }
            /*                                   //implementar refresh segunda fase
            if(expiration != null && expiration.isBefore(Instant.now())) {
                Map<String, String> claims = new HashMap<>();
                AuthToken generator = new AuthToken();
                claims.put("user", String.valueOf(decoded.getClaim("user")));
                claims.put("role", String.valueOf(decoded.getClaim("role")));
                claims.put("status", String.valueOf(decoded.getClaim("status")));

                String newToken = generator.generateToken(claims);
                decoded = JWT.decode(newToken);
            }
            */
            return decoded;
        } catch (Exception e) {
            throw new InvalidParameterException("Token validation failed: " + e.getMessage());
        }
    }
    public DecodedJWT checkToken(HttpServletRequest request){
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

        return this.validateToken(codedToken);
    }

}