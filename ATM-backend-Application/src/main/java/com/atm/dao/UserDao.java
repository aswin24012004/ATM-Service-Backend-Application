package com.atm.dao;

import com.atm.model.User;
import com.atm.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class UserDao {

	public User findByUsername(String username) {
	    try (Connection conn = DBUtil.getConnection();
	         PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
	        stmt.setString(1, username);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            int id = rs.getInt("id");
	            String pinHash = rs.getString("pin_hash");
	            String role = rs.getString("role");
	            double balance = rs.getDouble("balance");

	            User user = new User(id, username, pinHash, role, balance,rs.getString("phone_number"),rs.getString("email"));
	            
	            return user;
	        }
	    } catch (SQLException e) {
	        throw new RuntimeException(e);
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
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
        	System.err.println("Exception: "+e);
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
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
        	System.err.println("Exception: "+e);
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
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
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
        } catch (SQLException e) {
        	System.err.println("Exception: "+e);
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
        } catch (SQLException e) {
        	System.err.println("Exception: "+e);
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
            return true;
        } catch (SQLException e) {
        	System.err.println("Exception: "+e);
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
                map.put("role", rs.getString("role"));
                map.put("balance", rs.getDouble("balance"));
                map.put("phone_number", rs.getString("phone_number"));
                map.put("email", rs.getString("email"));
                result.add(map);
            }
        } catch (SQLException e) {
        	System.err.println("Exception: "+e);
        }
        return result;
    }
}

