package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import com.atm.service.EmailService;
import io.jsonwebtoken.Claims;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/deposit")
public class DepositServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DepositServlet.class);
    private final EmailService emailService = new EmailService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Validate JWT
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Missing or invalid Authorization header\"}");
            logger.warn("Deposit attempt denied: missing/invalid auth header");
            return;
        }

        String token = authHeader.substring(7);
        if (!TokenUtil.validateToken(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Invalid token\"}");
            logger.warn("Deposit attempt denied: invalid token");
            return;
        }

        Claims claims = TokenUtil.getClaims(token);
        String username = claims.getSubject();
        double amount = Double.parseDouble(req.getParameter("amount"));

        JdbcTemplate jdbc = DBUtil.getJdbcTemplate();

        // Update user balance
        jdbc.update("UPDATE users SET balance = balance + ? WHERE username=?", amount, username);

        // Log transaction
        jdbc.update("INSERT INTO transactions(username, type, amount) VALUES(?, ?, ?)", username, "deposit", amount);

        // Get updated balance
        Double newBalance = jdbc.queryForObject(
            "SELECT balance FROM users WHERE username=?",
            new Object[]{username},
            Double.class
        );

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        out.println("{\"status\":\"success\",\"newBalance\":" + newBalance + "}");
        logger.info("Deposit successful: user={}, amount={}, newBalance={}", username, amount, newBalance);

        // Send transaction email
        try {
            String email = jdbc.queryForObject(
                "SELECT email FROM users WHERE username=?",
                new Object[]{username},
                String.class
            );
            emailService.sendEmail(email, "Deposit Alert",
                "Dear " + username + ",\nYou deposited Rs." + amount +
                ". Current balance: Rs." + newBalance);
        } catch (Exception e) {
            logger.error("Failed to send deposit email to user={}", username, e);
        }
    }
}
