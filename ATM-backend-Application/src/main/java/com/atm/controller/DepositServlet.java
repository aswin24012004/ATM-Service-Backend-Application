package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import com.atm.service.EmailService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/deposit")
public class DepositServlet extends HttpServlet {

    
	private static final Logger LOGGER = LoggerFactory.getLogger(DepositServlet.class);
    protected EmailService emailService;

    public DepositServlet() {
        this(new EmailService());
    }

    // Constructor for testing
    public DepositServlet(EmailService emailService) {
        this.emailService = emailService;
    }
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Validate JWT
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Missing or invalid Authorization header\"}");
            LOGGER.warn("Deposit attempt denied: missing/invalid auth header");
            return;
        }

        String token = authHeader.substring(7);
        if (!TokenUtil.validateToken(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Invalid token\"}");
            LOGGER.warn("Deposit attempt denied: invalid token");
            return;
        }

        Claims claims = TokenUtil.getClaims(token);
        String username = claims.getSubject();
        double amount = Double.parseDouble(req.getParameter("amount"));

        try (Connection conn = DBUtil.getConnection()) {
            // Update user balance
        	String updateQuery = "UPDATE users SET balance = balance + ? WHERE username=?";
        	String insertQuery = "INSERT INTO transactions(username, type, amount) VALUES(?, ?, ?)";
        	String readQuery = "SELECT balance FROM users WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                ps.setDouble(1, amount);
                ps.setString(2, username);
                ps.executeUpdate();
            }

            // Log transaction
            try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                ps.setString(1, username);
                ps.setString(2, "deposit");
                ps.setDouble(3, amount);
                ps.executeUpdate();
            }

            // Get updated balance
            Double newBalance = null;
            try (PreparedStatement ps = conn.prepareStatement(readQuery)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        newBalance = rs.getDouble("balance");
                    }
                }
            }

            res.setContentType("application/json");
            PrintWriter out = res.getWriter();
            if (newBalance != null) {
                out.println("{\"status\":\"success\",\"newBalance\":" + newBalance + "}");
                LOGGER.info("Deposit successful: user={}, amount={}, newBalance={}", username, amount, newBalance);

                // Send transaction email
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT email FROM users WHERE username=?")) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String email = rs.getString("email");
                            emailService.sendEmail(email, "Deposit Alert",
                                    "Dear " + username + ",\nYou deposited Rs." + amount +
                                            ". Current balance: Rs." + newBalance);
                        }
                    }
                } catch (Exception e) {
                	LOGGER.error("Failed to send deposit email to user={}", username, e);
                }
            } else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().println("{\"error\":\"User not found\"}");
                LOGGER.warn("Deposit failed: user={} not found", username);
            }
        } catch (Exception e) {
        	LOGGER.error("Error processing deposit for user={}", username, e);
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().println("{\"error\":\"Server error during deposit\"}");
        }
    }
}
