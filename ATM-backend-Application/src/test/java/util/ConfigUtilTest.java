package util;

import org.junit.jupiter.api.Test;
import com.atm.util.ConfigUtil;
import static org.junit.jupiter.api.Assertions.*;



class ConfigUtilTest {



    @Test
    void testGetNonExistingPropertyReturnsNull() {
        String value = ConfigUtil.get("non.existing.key");
        assertNull(value);
    }

    @Test
    void testGetWithDefaultValue() {
        String value = ConfigUtil.get("non.existing.key", "defaultValue");
        assertEquals("defaultValue", value);
    }

    @Test
    void testGetIntValid() {
        int port = ConfigUtil.getInt("server.port", 8080);
        assertEquals(8080, port);
    }

    @Test
    void testGetIntInvalidFallsBackToDefault() {
        int port = ConfigUtil.getInt("server.port", 1234);
        assertEquals(1234, port);
    }

    @Test
    void testGetBooleanTrue() {
        boolean enabled = ConfigUtil.getBoolean("feature.enabled", false);
        assertTrue(enabled);
    }

    @Test
    void testGetBooleanDefault() {
        boolean enabled = ConfigUtil.getBoolean("non.existing.boolean", true);
        assertTrue(enabled);
    }
}
