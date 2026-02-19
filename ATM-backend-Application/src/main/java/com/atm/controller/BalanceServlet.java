package com.atm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;

@WebServlet("/api/balance")
public class BalanceServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        String username = null;

        if (session != null) {
            username = (String) session.getAttribute("username");
        } else {
        	
            String token = req.getHeader("Authorization").substring(7);
            Claims claims = TokenUtil.getClaims(token);
            username = claims.getSubject();
        }

        JdbcTemplate jdbc = DBUtil.getJdbcTemplate();
        Double balance = jdbc.queryForObject(
            "SELECT balance FROM users WHERE username=?",
            new Object[]{username},
            Double.class
        );

        res.setContentType("application/json");
        res.getWriter().println("{\"username\":\"" + username + "\", \"balance\":" + balance + "}");
    }
}
