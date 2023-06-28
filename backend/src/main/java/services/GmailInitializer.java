package services;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import javax.inject.Singleton;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Singleton
public class GmailInitializer implements ServletContextListener {
    private static Gmail gmailService;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            gmailService = initializeGmailService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Gmail initializeGmailService() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("./credentials.json"))
                .createScoped(Collections.singleton(GmailScopes.GMAIL_SEND));
        return new Gmail.Builder(httpTransport,  GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("magikarp-fct")
                .build();
    }

    public static Gmail getGmailService() {
        return gmailService;
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // Clean up any resources if needed
    }
}
