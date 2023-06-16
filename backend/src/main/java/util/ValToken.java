package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import resources.LoginResource;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.security.InvalidParameterException;
import java.security.Signature;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;


public class ValToken{
    private static final Logger LOG = Logger.getLogger(ValToken.class.getName());
    private static final List<String> allowedIssuers = Collections.singletonList("https://universe-fct.oa.r.appspot.com/");

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public DecodedJWT validateToken(String token) {
        if (token == null)
            return null;
        DecodedJWT decoded = JWT.decode(token);
        Transaction txn = datastore.newTransaction();
        try {
            Key tokenKey = datastore.newKeyFactory().setKind("Token_Blacklist").newKey(decoded.getId());
            Entity blToken = txn.get(tokenKey);

            if (blToken != null) {
                txn.rollback();
                LOG.warning("Token invalid.");
                throw new InvalidParameterException("Token validation failed");
            }
        } finally{
            if (txn.isActive()) {
                txn.rollback();
            }
        }

        try {
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

    public DecodedJWT invalidateToken(HttpServletRequest request, HttpServletResponse response){
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

        assert token != null;
        return JWT.decode(token);
    }

}