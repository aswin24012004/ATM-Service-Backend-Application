package com.atm.util;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBUtil {
    private static HikariDataSource dataSource;

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

            // initialize hikari datasource
            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DBUtil", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource not initialized");
        }
        return dataSource.getConnection();
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
