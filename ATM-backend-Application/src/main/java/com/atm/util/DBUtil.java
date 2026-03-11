package com.atm.util;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBUtil {
    private static HikariDataSource dataSource;

    private static synchronized void init() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();

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

            dataSource = new HikariDataSource(config);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            init();
        }
        return dataSource.getConnection();
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            init();
        }
        return dataSource;
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
