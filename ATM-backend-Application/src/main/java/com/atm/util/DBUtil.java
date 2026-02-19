package com.atm.util;

import org.springframework.jdbc.core.JdbcTemplate;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


public class DBUtil {
    private static JdbcTemplate jdbcTemplate;

    static {
        try {
            HikariConfig config = new HikariConfig();
            
            config.setJdbcUrl(ConfigUtil.get("db.url"));
            config.setUsername(ConfigUtil.get("db.username"));
            config.setPassword(ConfigUtil.get("db.password"));
            config.setDriverClassName(ConfigUtil.get("db.driver"));
            config.setMaximumPoolSize(Integer.parseInt(ConfigUtil.get("db.pool.max", "10"))); // at a time to getting 10 connections
            config.setMinimumIdle(Integer.parseInt(ConfigUtil.get("db.pool.minIdle", "2"))); // it means minimum 2 connections

            HikariDataSource dataSource = new HikariDataSource(config);
            jdbcTemplate = new JdbcTemplate(dataSource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DBUtil", e);
        }
    }

    public static JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public static void shutdown() {
        if (jdbcTemplate != null) {
            ((HikariDataSource) jdbcTemplate.getDataSource()).close();
        }
    }
}
