package com.atm.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.atm.util.DBUtil;
import com.atm.util.HashUtil;
import com.atm.util.TokenUtil;
import com.atm.service.RegexService;
import com.atm.service.EmailService;
import io.jsonwebtoken.Claims;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@WebServlet("/api/admin")
public class AdminServlet extends HttpServlet {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminServlet.class);
    private final EmailService emailService;
    public AdminServlet() {
        this(new EmailService());
    }
    public AdminServlet(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String token = req.getHeader("Authorization").replace("Bearer ", "");
        Claims claims = TokenUtil.getClaims(token);
        String role = (String) claims.get("role");
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        if (!"ADMIN".equals(role)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("{\"error\":\"Access denied\"}");
            LOGGER.warn("Unauthorized access attempt by role={}", role);
            return;
        }
        String path = req.getParameter("path");
        try (Connection conn = DBUtil.getConnection()) {
            if ("users".equals(path)) {
                List<Map<String, Object>> users = new ArrayList<>();
                String sql = "SELECT id, username, role, balance, phone_number, email FROM users";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", rs.getInt("id"));
                        map.put("username", rs.getString("username"));
                        map.put("role", rs.getString("role"));
                        map.put("balance", rs.getDouble("balance"));
                        map.put("phone_number", rs.getString("phone_number"));
                        map.put("email", rs.getString("email"));
                        users.add(map);
                    }
                }
                out.println(new Gson().toJson(users));
                LOGGER.info("Admin retrieved user list");
            } 
            
            else if ("transactions".equals(path)) {
                List<Map<String, Object>> txns = new ArrayList<>();
                String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", rs.getInt("id"));
                        map.put("username", rs.getString("username"));
                        map.put("type", rs.getString("type"));
                        map.put("amount", rs.getDouble("amount"));
                        map.put("timestamp", rs.getTimestamp("timestamp"));
                        txns.add(map);
                    }
                }
                out.println(new Gson().toJson(txns));
                LOGGER.info("Admin retrieved transaction list");
            } 
            
            else if ("atm".equals(path)) {
                String sql = "SELECT balance FROM atm WHERE id=1";
                Map<String, Object> atm = new HashMap<>();
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double atmBalance = rs.getDouble("balance");
                        atm.put("balance", atmBalance);
                        out.println(new Gson().toJson(atm));
                        if (atmBalance < 5000) {
                        	LOGGER.warn("ATM balance low: {}", atmBalance);
                            try {
                                emailService.sendEmail("aswin.c201@gmail.com", "ATM Low Balance Alert",
                                        "ATM balance is below Rs.5000. Current balance: Rs." + atmBalance);
                            } catch (Exception e) {
                            	LOGGER.error("Failed to send ATM low balance alert", e);
                            }
                        }
                    }
                }
            } 
            
            else {
                out.println("{\"error\":\"Invalid Path\"}");
                LOGGER.error("Invalid path parameter: {}", path);
            }
        } catch (Exception e) {
        	LOGGER.error("Error in AdminServlet GET", e);
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\":\"Server error\"}");
        }
    }
    @Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String path = req.getParameter("path");
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        if ("insert".equals(path)) {
            String username = req.getParameter("username");
            String pin = req.getParameter("pin");
            String userRole = req.getParameter("role");
            String phone = req.getParameter("phone_number");
            String email = req.getParameter("email");
            LOGGER.info("User registration attempt: username={}, role={}", username, userRole);
            try (Connection conn = DBUtil.getConnection()) {
                if (!RegexService.isValidPhone(phone)) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"status\":\"error\",\"message\":\"Invalid phone number\"}");
                    LOGGER.warn("Invalid phone number provided: {}", phone);
                    return;
                }
                if (!RegexService.isValidEmail(email)) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"status\":\"error\",\"message\":\"Invalid email address\"}");
                    LOGGER.warn("Invalid email provided: {}", email);
                    return;
                }
                String normalizedPhone = RegexService.normalizePhone(phone);
                String hashedPin = HashUtil.hashPassword(pin);
                String sql = "INSERT INTO users(username, pin_hash, role, balance, phone_number, email) VALUES(?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, username);
                    ps.setString(2, hashedPin);
                    ps.setString(3, userRole.toUpperCase());
                    ps.setDouble(4, 0.0);
                    ps.setString(5, normalizedPhone);
                    ps.setString(6, email);
                    ps.executeUpdate();
                }
                out.println("{\"status\":\"success\",\"message\":\"User created successfully\"}");
                LOGGER.info("User created successfully: username={}", username);
                emailService.sendEmail(email, "Welcome to ATM System",
                        "Dear " + username + ",\nYour account has been created successfully.\nRole: " + userRole);
            } catch (Exception e) {
            	LOGGER.error("Error creating user: {}", username, e);
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"status\":\"error\",\"message\":\"User already exists or invalid input\"}");
            }
            
        }
    }
}