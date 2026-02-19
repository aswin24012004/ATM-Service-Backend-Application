package com.atm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.jdbc.core.JdbcTemplate;
import java.io.PrintWriter;


@WebServlet("/api/deposit")
public class DepositServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // check the  JWT
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

        // change type String -> Double deposit amount
        double amount = Double.parseDouble(req.getParameter("amount"));

        JdbcTemplate jdbc = DBUtil.getJdbcTemplate();

        // Update user balance
        jdbc.update("UPDATE users SET balance = balance + ? WHERE username=?", amount, username);

        // Log transaction in the user
        jdbc.update("INSERT INTO transactions(username, type, amount) VALUES(?, ?, ?)", username, "deposit", amount);

        // Get the  updated balance
        Double newBalance = jdbc.queryForObject(
                "SELECT balance FROM users WHERE username=?",
                new Object[]{username},
                Double.class
        );

        // Return JSON response
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        out.println("{\"status\":\"success\",\"newBalance\":" + newBalance + "}");
    }
}
