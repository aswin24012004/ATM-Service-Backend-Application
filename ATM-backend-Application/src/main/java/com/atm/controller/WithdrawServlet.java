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
import io.jsonwebtoken.Claims;

@WebServlet("/api/withdraw")
public class WithdrawServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private UserDao userDao = new UserDao();
    private ATMDao atmDao = new ATMDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Validate JWT
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
        String username = claims.getSubject();

        double amount = Double.parseDouble(req.getParameter("amount"));

     
        User user = userDao.findByUsername(username);
        ATM atm = atmDao.getATMById(1);

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        // Check ATM balance have minimum balance
        if (atm.getTotalBalance() < 10000) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"status\":\"error\",\"message\":\"ATM balance is low (<10k). Admin must add funds.\"}");
            return;
        }

        // Check if ATM has enough amount
        if (atm.getTotalBalance() < amount) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"status\":\"error\",\"message\":\"ATM has insufficient cash.\"}");
            return;
        }

        // Check if user has enough balance
        if (user.getBalance() < amount) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"status\":\"error\",\"message\":\"Insufficient user funds.\"}");
            return;
        }

        // Deduct balances
        userDao.updateBalance(username, user.getBalance() - amount);
        atmDao.checkBalance(1,amount);

        // Log transaction
        userDao.logTransaction(username, "withdraw", amount);

        // Fetch updated balances
        User updatedUser = userDao.findByUsername(username);
        ATM updatedAtm = atmDao.getATMById(1);

        out.println("{\"status\":\"success\",\"newUserBalance\":" + updatedUser.getBalance() +
                    ", \"newAtmBalance\":" + updatedAtm.getTotalBalance() + "}");
    }
}
