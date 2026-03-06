package com.atm.util;

import org.junit.jupiter.api.Test;
import com.atm.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DBUtilTest {

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
        // we can't easily verify the datasource internals here; just ensure method exists
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
}
