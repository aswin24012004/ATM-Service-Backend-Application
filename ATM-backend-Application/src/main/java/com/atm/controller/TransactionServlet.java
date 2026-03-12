package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.atm.dao.TransactionDao;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/transactions")
public class TransactionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServlet.class);

    protected TransactionDao txDao;

    public TransactionServlet() {
        this.txDao = new TransactionDao();
    }

    public TransactionServlet(TransactionDao txDao) {
        this.txDao = txDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Missing or invalid Authorization header\"}");
            LOGGER.warn("Transaction fetch denied: missing/invalid auth header");
            return;
        }

        String token = authHeader.substring(7);
        if (!TokenUtil.validateToken(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().println("{\"error\":\"Invalid token\"}");
            LOGGER.warn("Transaction fetch denied: invalid token");
            return;
        }

        Claims claims = TokenUtil.getClaims(token);
        String username = claims.getSubject();
        String role = (String) claims.get("role");

        List<?> transactions;
        if ("ADMIN".equals(role)) {
            transactions = txDao.findAll();
            LOGGER.info("Admin retrieved all transactions");
        } else {
            transactions = txDao.findByUsername(username);
            LOGGER.info("User {} retrieved their transactions", username);
        }

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        out.println(new Gson().toJson(transactions));
    }
}
