package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import com.atm.util.TokenUtil;
import com.atm.service.EmailService;
import com.atm.dao.UserDao;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/deposit")
public class DepositServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DepositServlet.class);
    protected UserDao userDao = new UserDao();
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

        // Update user balance and log transaction using injected dao
        double oldBal = this.userDao.getBalance(username);
        userDao.updateBalance(username, oldBal + amount);
        userDao.logTransaction(username, "deposit", amount);

        // Get updated balance
        Double newBalance = userDao.getBalance(username);

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        out.println("{\"status\":\"success\",\"newBalance\":" + newBalance + "}");
        logger.info("Deposit successful: user={}, amount={}, newBalance={}", username, amount, newBalance);

        // Send transaction email
        try {
            String email = userDao.getEmail(username);
            emailService.sendEmail(email, "Deposit Alert",
                "Dear " + username + ",\nYou deposited Rs." + amount +
                ". Current balance: Rs." + newBalance);
        } catch (Exception e) {
            logger.error("Failed to send deposit email to user={}", username, e);
        }
    }
}
