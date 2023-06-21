package services;

import javax.inject.Singleton;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.security.SecureRandom;
import java.util.Random;

@Singleton
public class ServerKeys implements ServletContextListener {
    private static final int BIT_LENGTH = 512;
    private static final String[] keys = new String[10];
    private static final Random random = new Random();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < 10; i++) {
            byte[] randomBytes = new byte[BIT_LENGTH / 8];
            secureRandom.nextBytes(randomBytes);

            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : randomBytes) {
                stringBuilder.append(String.format("%02x", b));
            }

            keys[i] = stringBuilder.toString();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    public static String[] getServerKeys() {
        return keys;
    }

    public static String getRandomKey() {
        return keys[random.nextInt(10)];
    }
}

