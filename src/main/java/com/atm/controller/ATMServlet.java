package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import com.atm.dao.ATMDao;
import com.atm.model.ATM;
import com.atm.util.TokenUtil;
import com.atm.service.EmailService;
import com.atm.util.ConfigUtil;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/atm")
public class ATMServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ATMServlet.class);
    protected ATMDao atmDao = new ATMDao();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Missing or invalid Authorization header\"}");
            logger.warn("ATM balance request denied: missing/invalid auth header");
            return;
        }

        String token = authHeader.substring(7);
        if (!TokenUtil.validateToken(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Invalid token\"}");
            logger.warn("ATM balance request denied: invalid token");
            return;
        }

        Claims claims = TokenUtil.getClaims(token);
        String role = (String) claims.get("role");

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        if (!"ADMIN".equals(role)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("{\"error\":\"Access denied. Only Admin can view ATM balance.\"}");
            logger.warn("Unauthorized ATM balance access attempt by role={}", role);
            return;
        }

        ATM atm = atmDao.getATMById(1);
        out.println("{\"atmBalance\":" + atm.getTotalBalance() + "}");

        if (atm.getTotalBalance() < 10000) {
            out.println("{\"warning\":\"ATM balance is low (<10k). Admin must add funds.\"}");
            logger.warn("ATM balance low: {}", atm.getTotalBalance());

            try {
//            	send the Email on the Admin
                String adminEmail = ConfigUtil.get("mail.username"); 
                emailService.sendEmail(adminEmail, "ATM Low Balance Alert",
                    "ATM balance is below Rs.10,000. Current balance: Rs." + atm.getTotalBalance());
            } catch (Exception e) {
                logger.error("Failed to send ATM low balance alert", e);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Missing or invalid Authorization header\"}");
            return;
        }

        String token = authHeader.substring(7);
        if (!TokenUtil.validateToken(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Invalid token\"}");
            return;
        }

        Claims claims = TokenUtil.getClaims(token);
        String role = (String) claims.get("role");

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        if (!"ADMIN".equals(role)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("{\"error\":\"Access denied. Only Admin can add ATM funds.\"}");
            logger.warn("Unauthorized ATM fund addition attempt by role={}", role);
            return;
        }

        double amount = Double.parseDouble(req.getParameter("amount"));
        atmDao.addFunds(1, amount);

        ATM atm = atmDao.getATMById(1);
        out.println("{\"status\":\"success\",\"newAtmBalance\":" + atm.getTotalBalance() + "}");
        logger.info("ATM funds added: amount={}, newBalance={}", amount, atm.getTotalBalance());
    }
}
