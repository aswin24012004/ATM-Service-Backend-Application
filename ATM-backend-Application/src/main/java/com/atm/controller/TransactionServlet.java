package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import com.atm.dao.TransactionDao;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/transactions")
public class TransactionServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(TransactionServlet.class);
	protected TransactionDao txDao = new TransactionDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
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

        List<?> transactions;

        if ("ADMIN".equals(role)) {
            transactions = txDao.findAll();
            logger.info("Admin retrieved all transactions");
        } else {
            transactions = txDao.findByUsername(username);
            logger.info("User {} retrieved their transactions", username);
        }

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        out.println(new Gson().toJson(transactions));
    }
}
