package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/transactions")
public class TransactionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(TransactionServlet.class);

    @Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Validate JWT
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Missing or invalid Authorization header\"}");
            logger.warn("Transaction fetch denied: missing/invalid auth header");
            return;
        }

        String token = authHeader.substring(7);
        if (!TokenUtil.validateToken(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Invalid token\"}");
            logger.warn("Transaction fetch denied: invalid token");
            return;
        }

        Claims claims = TokenUtil.getClaims(token);
        String username = claims.getSubject();
        String role = (String) claims.get("role");

        List<Map<String, Object>> transactions = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection()) {
            String sql;
            if ("ADMIN".equals(role)) {
                sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
                logger.info("Admin retrieved all transactions");
            } else {
                sql = "SELECT * FROM transactions WHERE username=? ORDER BY timestamp DESC";
                logger.info("User {} retrieved their transactions", username);
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (!"ADMIN".equals(role)) {
                    ps.setString(1, username);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> txn = new HashMap<>();
                        txn.put("id", rs.getInt("id"));
                        txn.put("username", rs.getString("username"));
                        txn.put("type", rs.getString("type"));
                        txn.put("amount", rs.getDouble("amount"));
                        txn.put("timestamp", rs.getTimestamp("timestamp"));
                        transactions.add(txn);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error retrieving transactions for user={}", username, e);
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().println("{\"error\":\"Unable to retrieve transactions\"}");
            return;
        }

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        out.println(new Gson().toJson(transactions));
    }
}
