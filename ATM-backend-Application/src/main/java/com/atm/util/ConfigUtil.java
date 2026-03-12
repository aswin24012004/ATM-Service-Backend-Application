package com.atm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public final class ConfigUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);
    private static final Properties props = new Properties();

    // Private constructor to prevent instantiation
    private ConfigUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    static {
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties not found in classpath");
            }
            props.load(input);
        } catch (Exception e) {
            // Either log or rethrow with a dedicated exception
        	LOGGER.error("Failed to load application.properties", e);
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key));
        } catch (Exception e) {
        	LOGGER.warn("Invalid integer for key '{}', using default {}", key, defaultValue);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        } else {
            return defaultValue;
        }
    }
}
