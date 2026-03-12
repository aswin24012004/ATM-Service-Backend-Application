package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import com.atm.dao.UserDao;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/balance")
public class BalanceServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(BalanceServlet.class);

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

        UserDao userDao = new UserDao();
        Double balance = userDao.getBalance(username);

        res.setContentType("application/json");
        res.getWriter().println("{\"username\":\"" + username + "\", \"balance\":" + balance + "}");
        LOGGER.info("Balance check: user={}, balance={}", username, balance);
    }
}
