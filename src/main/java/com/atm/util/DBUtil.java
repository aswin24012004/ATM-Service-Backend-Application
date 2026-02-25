package com.atm.util;

import org.springframework.jdbc.core.JdbcTemplate;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBUtil {
    private static JdbcTemplate jdbcTemplate;

    static {
        try {
            HikariConfig config = new HikariConfig();

            // Basic DB connection settings
            config.setJdbcUrl(ConfigUtil.get("db.url"));
            config.setUsername(ConfigUtil.get("db.username"));
            config.setPassword(ConfigUtil.get("db.password"));
            config.setDriverClassName(ConfigUtil.get("db.driver"));

            String maxPoolStr = ConfigUtil.get("db.pool.max", "10").trim();
            String minIdleStr = ConfigUtil.get("db.pool.minIdle", "2").trim();

            try {
                config.setMaximumPoolSize(Integer.parseInt(maxPoolStr));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid value for db.pool.max: " + maxPoolStr, e);
            }

            try {
                config.setMinimumIdle(Integer.parseInt(minIdleStr));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid value for db.pool.minIdle: " + minIdleStr, e);
            }

            // Initialize datasource and JdbcTemplate
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
