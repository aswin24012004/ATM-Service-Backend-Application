package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import org.springframework.jdbc.core.JdbcTemplate;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        String username;

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
        logger.info("Balance check: user={}, balance={}", username, balance);
    }
}
