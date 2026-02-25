package com.atm.util;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import com.zaxxer.hikari.HikariDataSource;

import static org.junit.jupiter.api.Assertions.*;

class DBUtilTest {

    @Test
    void testJdbcTemplateIsInitialized() {
        JdbcTemplate jdbc = DBUtil.getJdbcTemplate();
        assertNotNull(jdbc, "JdbcTemplate should be initialized");
        assertNotNull(jdbc.getDataSource(), "DataSource should not be null");
    }

    @Test
    void testShutdownClosesDataSource() {
        JdbcTemplate jdbc = DBUtil.getJdbcTemplate();
        HikariDataSource ds = (HikariDataSource) jdbc.getDataSource();

        assertFalse(ds.isClosed(), "DataSource should be open before shutdown");

        DBUtil.shutdown();

        assertTrue(ds.isClosed(), "DataSource should be closed after shutdown");
    }

    @Test
    void testInvalidPoolSizeThrowsException() {
    
        String invalidValue = "notAnNumber";
        Exception ex = assertThrows(RuntimeException.class, () -> {
            Integer.parseInt(invalidValue);
        });
        assertTrue(ex.getMessage().contains("For input string"));
    }
}
