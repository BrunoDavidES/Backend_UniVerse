package utils;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

import static utils.Constants.*;

public class FirebaseAuth {
    private static final com.google.firebase.auth.FirebaseAuth firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();

    public FirebaseAuth() {}

    public static FirebaseToken authenticateToken(String token) {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token, true);

            if( ! firebaseAuth.getUser(decodedToken.getUid()).isEmailVerified()) {
                return null;
            }
            return decodedToken;
        } catch (FirebaseAuthException e) {
            return null;
        }
    }

    public static String getRole(UserRecord user) { return String.valueOf(user.getCustomClaims().get(ROLE));}

    public static String getRole(FirebaseToken token) {
        return String.valueOf(token.getClaims().get(ROLE));
    }
}
