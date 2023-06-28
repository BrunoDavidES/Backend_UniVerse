package util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

public class AuthToken {
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public AuthToken() {}

    public static FirebaseToken authenticateToken(String token) {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);

            if( ! firebaseAuth.getUser(decodedToken.getUid()).isEmailVerified()) {
                return null;
            }
            return decodedToken;
        } catch (FirebaseAuthException e) {
            return null;
        }
    }

    public static String getRole(FirebaseToken token) {
        return (String) token.getClaims().get("role");
    }
}
