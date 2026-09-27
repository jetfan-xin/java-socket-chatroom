package dev.jingfan.jchat.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/** Loads the shared client and server settings from the classpath. */
public final class AppConfig {
    private static final String RESOURCE_NAME = "serverconfig.properties";

    private AppConfig() {
    }

    public static Properties load() {
        Properties properties = new Properties();
        properties.setProperty("ip", "localhost");
        properties.setProperty("port", "6666");
        properties.setProperty("dbpath", "data/users.db");

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream input = loader.getResourceAsStream(RESOURCE_NAME)) {
            if (input == null) {
                throw new IllegalStateException("Missing classpath resource: " + RESOURCE_NAME);
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not load " + RESOURCE_NAME, exception);
        }
    }
}
