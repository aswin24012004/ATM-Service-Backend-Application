package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import com.atm.dao.ATMDao;
import com.atm.dao.UserDao;
import com.atm.model.ATM;
import com.atm.model.User;
import com.atm.util.TokenUtil;
import com.atm.service.EmailService;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/withdraw")
public class WithdrawServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(WithdrawServlet.class);
	protected UserDao userDao;
    protected ATMDao atmDao;
    protected EmailService emailService;

    public WithdrawServlet() {
        this(new UserDao(), new ATMDao(), new EmailService());
    }

    // Constructor for testing
    public WithdrawServlet(UserDao userDao, ATMDao atmDao, EmailService emailService) {
        this.userDao = userDao;
        this.atmDao = atmDao;
        this.emailService = emailService;
    }
    @Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Validate JWT
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Missing or invalid Authorization header\"}");
            logger.warn("Withdraw attempt denied: missing/invalid auth header");
            return;
        }

        String token = authHeader.substring(7);
        if (!TokenUtil.validateToken(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Invalid token\"}");
            logger.warn("Withdraw attempt denied: invalid token");
            return;
        }

        Claims claims = TokenUtil.getClaims(token);
        String username = claims.getSubject();
        double amount = Double.parseDouble(req.getParameter("amount"));

        User user = userDao.findByUsername(username);
        ATM atm = atmDao.getATMById(1);

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        // ATM minimum balance check
        if (atm.getTotalBalance() < 10000) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"status\":\"error\",\"message\":\"ATM balance is low (<10k). Admin must add funds.\"}");
            logger.warn("ATM balance too low for withdrawal: {}", atm.getTotalBalance());
            return;
        }

        // ATM have enough cash
        if (atm.getTotalBalance() < amount) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"status\":\"error\",\"message\":\"ATM has insufficient cash.\"}");
            logger.warn("ATM insufficient cash for withdrawal: requested={}, available={}", amount, atm.getTotalBalance());
            return;
        }

        // User has enough Amount
        if (user.getBalance() < amount) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"status\":\"error\",\"message\":\"Insufficient user funds.\"}");
            logger.warn("User {} insufficient funds: requested={}, balance={}", username, amount, user.getBalance());
            return;
        }

        // Deduct balances
        userDao.updateBalance(username, user.getBalance() - amount);
        atmDao.checkBalance(1, amount);

        // Log transaction
        userDao.logTransaction(username, "withdraw", amount);

        // Fetch updated balances
        User updatedUser = userDao.findByUsername(username);
        ATM updatedAtm = atmDao.getATMById(1);

        out.println("{\"status\":\"success\",\"newUserBalance\":" + updatedUser.getBalance() +
                    ", \"newAtmBalance\":" + updatedAtm.getTotalBalance() + "}");
        logger.info("Withdrawal successful: user={}, amount={}, newBalance={}", username, amount, updatedUser.getBalance());

        // Send transaction email
        try {
            emailService.sendEmail(updatedUser.getEmail(), "Withdrawal Alert",
                "Dear " + updatedUser.getUsername() + ",\nYou withdrew Rs." + amount +
                ". Current balance: Rs." + updatedUser.getBalance());
        } catch (Exception e) {
            logger.error("Failed to send withdrawal email to user={}", username, e);
        }
    }
}
