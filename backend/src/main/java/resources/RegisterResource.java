package resources;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.database.FirebaseDatabase;
import util.UserData;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import com.google.api.services.gmail.model.Message;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import static javax.mail.Message.RecipientType.TO;
import static util.Constants.*;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
    private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static final String EMAIL_ADDRESS = "magikarpfct@gmail.com";

    public RegisterResource() {}

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(UserData data) throws Exception {
        LOG.fine("Attempt to register user: " + data.username);

        if (!data.validateRegister()) {
            LOG.warning(MISSING_OR_WRONG_PARAMETER);
            return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_OR_WRONG_PARAMETER).build();
        }

        // Firebase Register
        Response response = firebaseRegister(data.username, data.email, data.password, data.name, data.getRole());
        if(response.getStatus() == Response.Status.OK.getStatusCode()) {

            // Datastore Register
            response = datastoreRegister(data.username, data.email, data.name, data.license_plate);
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                emailVerification(data.email);
            }
            else {
                firebaseAuth.deleteUser(data.username);
            }
        }

        return response;
    }

    public void emailVerification(String email) throws Exception {
        String link = firebaseAuth.generateEmailVerificationLink(email);

        sendEmail(email, EMAIL_ADDRESS, link);

    }

    public static void sendEmail(String fromEmailAddress, String toEmailAddress, String link) throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(httpTransport, GsonFactory.getDefaultInstance(), getCredentials(httpTransport))
                .setApplicationName("Test Mail")
                .build();

        // Create the email content
        String messageSubject = "Test message";
        String bodyText = "lorem ipsum.";

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(EMAIL_ADDRESS));
        email.addRecipient(TO, new InternetAddress(toEmailAddress));
        email.setSubject(messageSubject);
        email.setText(bodyText);

        // Encode and wrap the MIME message into a gmail message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message message = new Message();
        message.setRaw(encodedEmail);

        try {
            // Create send message
            message = service.users().messages().send("me", message).execute();
            System.out.println("Message id: " + message.getId());
            System.out.println(message.toPrettyString());
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                System.err.println("Unable to send message: " + e.getDetails());
            } else {
                throw e;
            }
        }

    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {

        InputStream in = RegisterResource.class.getResourceAsStream("/tokens/client_secret.json");
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + "/tokens/client_secret.json");
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(GsonFactory.getDefaultInstance(), new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), clientSecrets, Set.of(GmailScopes.GMAIL_SEND))
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get("tokens").toFile()))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    private Response firebaseRegister(String username, String email, String password, String name, String role) {
        try {
            UserRecord userRecord = firebaseAuth.createUser( new CreateRequest()
                    .setUid(username)
                    .setEmail(email)
                    .setEmailVerified(false)
                    .setPassword(password)
                    .setDisplayName(name)
                    .setDisabled(true)
            );

            Map<String, Object> customClaims = new HashMap<>();
            customClaims.put("role", role);
            firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

            LOG.info("User registered in Firebase: " + userRecord.getUid());
            return Response.ok(userRecord).build();
        } catch (FirebaseAuthException e) {
            LOG.warning("Firebase registration failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Firebase registration failed: " + e.getMessage()).build();
        }
    }

    private Response datastoreRegister(String username, String email, String name, String license_plate) {
        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);

            if(license_plate == null)
                license_plate = UNREGISTERED;

            Entity user = Entity.newBuilder(userKey)
                    .set("email", email)
                    .set("name", name)
                    .set("license_plate", license_plate)
                    .set("job_list", "")
                    .set("personal_event_list", "")  //#string%string%string%string#string%...
                    .set("time_lastupdate", Timestamp.now())
                    .build();
            txn.add(user);

            LOG.info("User registered in datastore " + username);
            txn.commit();
            return Response.ok(user).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }


}
