package com.atm.dao;

import com.atm.model.User;
import com.atm.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    // Constants for column names
    private static final String COL_EMAIL = "email";
    private static final String COL_PHONE = "phone_number";
    private static final String COL_ROLE = "role";
    private static final String COL_BALANCE = "balance";

    public User findByUsername(String username) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    username,
                    rs.getString("pin_hash"),
                    rs.getString(COL_ROLE),
                    rs.getDouble(COL_BALANCE),
                    rs.getString(COL_PHONE),
                    rs.getString(COL_EMAIL)
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username: " + username, e);
        }
        return null;
    }

    public String getRole(String username) {
        String sql = "SELECT role FROM users WHERE username=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(COL_ROLE);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching role for user {}: {}", username, e.getMessage(), e);
        }
        return null;
    }

    public String getEmail(String username) {
        String sql = "SELECT email FROM users WHERE username=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(COL_EMAIL);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching email for user {}: {}", username, e.getMessage(), e);
        }
        return null;
    }

    public double getBalance(String username) {
        String sql = "SELECT balance FROM users WHERE username=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(COL_BALANCE);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching balance for user {}: {}", username, e.getMessage(), e);
        }
        return 0.0;
    }

    public void updateBalance(String username, double balance) {
        String sql = "UPDATE users SET balance=? WHERE username=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, balance);
            ps.setString(2, username);
            ps.executeUpdate();
            logger.info("Updated balance for user {} to {}", username, balance);
        } catch (SQLException e) {
            logger.error("Error updating balance for user {}: {}", username, e.getMessage(), e);
        }
    }

    public void logTransaction(String username, String type, double amount) {
        String sql = "INSERT INTO transactions(username, type, amount) VALUES(?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.executeUpdate();
            logger.info("Logged transaction for user {}: {} {}", username, type, amount);
        } catch (SQLException e) {
            logger.error("Error logging transaction for user {}: {}", username, e.getMessage(), e);
        }
    }

    public boolean createUser(String username, String pinHash, String role, double balance, String phone, String email) {
        String sql = "INSERT INTO users(username, pin_hash, role, balance, phone_number, email) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, pinHash);
            ps.setString(3, role);
            ps.setDouble(4, balance);
            ps.setString(5, phone);
            ps.setString(6, email);
            ps.executeUpdate();
            logger.info("Created user {}", username);
            return true;
        } catch (SQLException e) {
            logger.error("Error creating user {}: {}", username, e.getMessage(), e);
            return false;
        }
    }

    public List<Map<String, Object>> getAllUsers() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT id, username, role, balance, phone_number, email FROM users";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("username", rs.getString("username"));
                map.put(COL_ROLE, rs.getString(COL_ROLE));
                map.put(COL_BALANCE, rs.getDouble(COL_BALANCE));
                map.put(COL_PHONE, rs.getString(COL_PHONE));
                map.put(COL_EMAIL, rs.getString(COL_EMAIL));
                result.add(map);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all users: {}", e.getMessage(), e);
        }
        return result;
    }
}
