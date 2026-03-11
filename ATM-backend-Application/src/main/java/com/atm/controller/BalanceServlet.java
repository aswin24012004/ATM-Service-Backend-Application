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
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/balance")
public class BalanceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(BalanceServlet.class);

    @Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        String username;

        if (session != null) {
            username = (String) session.getAttribute("username");
        } else {
            String token = req.getHeader("Authorization").substring(7);
            Claims claims = TokenUtil.getClaims(token);
            username = claims.getSubject();
        }

        Double balance = null;
        String sql = "SELECT balance FROM users WHERE username=?" ;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    balance = rs.getDouble("balance");
                }
            }
        } catch (Exception e) {
            logger.error("Error retrieving balance for user={}", username, e);
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().println("{\"error\":\"Unable to retrieve balance\"}");
            return;
        }

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        if (balance != null) {
            out.println("{\"username\":\"" + username + "\", \"balance\":" + balance + "}");
            logger.info("Balance check: user={}, balance={}", username, balance);
        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("{\"error\":\"User not found\"}");
            logger.warn("Balance check failed: user={} not found", username);
        }
    }
}
