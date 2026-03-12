package com.atm.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigUtilTest {

    @Test
    void testGetStringProperty() {
        String dbUrl = ConfigUtil.get("db.url");
        assertNotNull(dbUrl, "db.url should not be null");
        assertTrue(dbUrl.startsWith("jdbc"), "db.url should start with jdbc");
    }

    @Test
    void testGetStringPropertyWithDefault() {
        String value = ConfigUtil.get("non.existent.key", "defaultValue");
        assertEquals("defaultValue", value, "Should return default value when key is missing");
    }

    @Test
    void testGetIntProperty() {
        int maxConnections = ConfigUtil.getInt("max.connections", 10);
        assertTrue(maxConnections > 0, "max.connections should be positive");
    }

    @Test
    void testGetIntPropertyWithDefault() {
        int value = ConfigUtil.getInt("non.existent.int", 42);
        assertEquals(42, value, "Should return default int when key is missing");
    }

    @Test
    void testGetBooleanProperty() {
        boolean featureEnabled = ConfigUtil.getBoolean("feature.enabled", true);
        assertTrue(featureEnabled, "feature.enabled should be true");
    }

    @Test
    void testGetBooleanPropertyWithDefault() {
        boolean value = ConfigUtil.getBoolean("non.existent.boolean", true);
        assertTrue(value, "Should return default boolean when key is missing");
    }
}
