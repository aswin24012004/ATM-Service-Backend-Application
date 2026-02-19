package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import com.google.gson.Gson;

@WebServlet("/api/transactions")
public class TransactionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
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
        String role = (String) claims.get("role");

        JdbcTemplate jdbc = DBUtil.getJdbcTemplate();
        List<Map<String, Object>> transactions;

        // Admin sees all transactions
        if ("ADMIN".equals(role)) {
            transactions = jdbc.queryForList("SELECT * FROM transactions ORDER BY timestamp DESC");
        } else {
       // 	User sees only their own
            transactions = jdbc.queryForList(
                "SELECT * FROM transactions WHERE username=? ORDER BY timestamp DESC", username
            );
        }

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        out.println(new Gson().toJson(transactions));
    }
}
