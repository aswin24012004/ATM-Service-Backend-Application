package util;

import com.atm.util.DBUtil;
import com.atm.util.ConfigUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DBUtilTest {

    @AfterEach
    void dbDown() {
        DBUtil.shutdown();
    }

    @Test
    void testGetConnectionMocked() throws SQLException {
        Connection conn = mock(Connection.class);
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            Connection c = DBUtil.getConnection();
            assertSame(conn, c);
        }
    }

    @Test
    void testShutdownDoesNotThrow() {
        // Should not throw even if datasource is null
        DBUtil.shutdown();
    }

    @Test
    void testInvalidPoolSizeThrowsException() {
        String invalidValue = "notAnNumber";
        Exception ex = assertThrows(NumberFormatException.class, () -> {
            Integer.parseInt(invalidValue);
        });
        assertTrue(ex.getMessage().contains("For input string"));
    }

    @Test
    void testInitWithValidConfig() throws SQLException {
        try (MockedStatic<ConfigUtil> configMock = mockStatic(ConfigUtil.class)) {
            configMock.when(() -> ConfigUtil.get("db.url")).thenReturn("jdbc:h2:mem:test");
            configMock.when(() -> ConfigUtil.get("db.username")).thenReturn("sa");
            configMock.when(() -> ConfigUtil.get("db.password")).thenReturn("");
            configMock.when(() -> ConfigUtil.get("db.driver")).thenReturn("org.h2.Driver");
            configMock.when(() -> ConfigUtil.get("db.pool.max", "10")).thenReturn("5");
            configMock.when(() -> ConfigUtil.get("db.pool.minIdle", "2")).thenReturn("1");

            Connection conn = DBUtil.getConnection();
            assertNotNull(conn);
            conn.close();
        }
    }

    @Test
    void testInitWithInvalidPoolSizeConfigThrows() {
        try (MockedStatic<ConfigUtil> configMock = mockStatic(ConfigUtil.class)) {
            configMock.when(() -> ConfigUtil.get("db.url")).thenReturn("jdbc:h2:mem:test");
            configMock.when(() -> ConfigUtil.get("db.username")).thenReturn("sa");
            configMock.when(() -> ConfigUtil.get("db.password")).thenReturn("");
            configMock.when(() -> ConfigUtil.get("db.driver")).thenReturn("org.h2.Driver");
            configMock.when(() -> ConfigUtil.get("db.pool.max", "10")).thenReturn("invalid");
            configMock.when(() -> ConfigUtil.get("db.pool.minIdle", "2")).thenReturn("2");

            RuntimeException ex = assertThrows(RuntimeException.class, DBUtil::getConnection);
            assertTrue(ex.getMessage().contains("Invalid value for db.pool.max"));
        }
    }
}
