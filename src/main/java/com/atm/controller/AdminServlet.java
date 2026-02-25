package com.atm.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

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
import org.springframework.jdbc.core.JdbcTemplate;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/admin")
public class AdminServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(AdminServlet.class);
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String token = req.getHeader("Authorization").replace("Bearer ", "");
        Claims claims = TokenUtil.getClaims(token);
        String role = (String) claims.get("role");

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        if (!"ADMIN".equals(role)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("{\"error\":\"Access denied\"}");
            logger.warn("Unauthorized access attempt by role={}", role);
            return;
        }

        String path = req.getParameter("path");
        JdbcTemplate jdbc = DBUtil.getJdbcTemplate();

        if ("users".equals(path)) {
            List<Map<String, Object>> users = jdbc.queryForList("SELECT id, username, role, balance, phone_number, email FROM users");
            out.println(new Gson().toJson(users));
            logger.info("Admin retrieved user list");

        } else if ("transactions".equals(path)) {
            List<Map<String, Object>> txns = jdbc.queryForList("SELECT * FROM transactions ORDER BY timestamp DESC");
            out.println(new Gson().toJson(txns));
            logger.info("Admin retrieved transaction list");

        } else if ("atm".equals(path)) {
            Map<String, Object> atm = jdbc.queryForMap("SELECT balance FROM atm WHERE id=1");
            double atmBalance = (double) atm.get("balance");
            out.println(new Gson().toJson(atm));

            if (atmBalance < 5000) {
                logger.warn("ATM balance low: {}", atmBalance);
                try {
                    emailService.sendEmail("admin@atm-system.com", "ATM Low Balance Alert",
                        "ATM balance is below Rs.5000. Current balance: Rs." + atmBalance);
                } catch (Exception e) {
                    logger.error("Failed to send ATM low balance alert", e);
                }
            }

        } else {
            out.println("{\"error\":\"Invalid Path\"}");
            logger.error("Invalid path parameter: {}", path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String path = req.getParameter("path");
        JdbcTemplate jdbc = DBUtil.getJdbcTemplate();
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        if ("insert".equals(path)) {
            String username = req.getParameter("username");
            String pin = req.getParameter("pin");
            String userRole = req.getParameter("role");
            String phone = req.getParameter("phone_number");
            String email = req.getParameter("email");

            logger.info("User registration attempt: username={}, role={}", username, userRole);

            try {
                if (!RegexService.isValidPhone(phone)) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"status\":\"error\",\"message\":\"Invalid phone number\"}");
                    logger.warn("Invalid phone number provided: {}", phone);
                    return;
                }
                if (!RegexService.isValidEmail(email)) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"status\":\"error\",\"message\":\"Invalid email address\"}");
                    logger.warn("Invalid email provided: {}", email);
                    return;
                }

                String normalizedPhone = RegexService.normalizePhone(phone);
                String hashedPin = HashUtil.hashPassword(pin);

                jdbc.update(
                    "INSERT INTO users(username, pin_hash, role, balance, phone_number, email) VALUES(?, ?, ?, ?, ?, ?)",
                    username, hashedPin, userRole.toUpperCase(), 0.0, normalizedPhone, email
                );

                out.println("{\"status\":\"success\",\"message\":\"User created successfully\"}");
                logger.info("User created successfully: username={}", username);

                // Sending the  Welcome Email
                emailService.sendEmail(email, "Welcome to ATM System",
                    "Dear " + username + ",\nYour account has been created successfully.\nRole: " + userRole);

            } catch (Exception e) {
                logger.error("Error creating user: {}", username, e);
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"status\":\"error\",\"message\":\"User already exists or invalid input\"}");
            }
        }
    }
}
