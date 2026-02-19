package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import com.atm.dao.ATMDao;
import com.atm.model.ATM;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;

@WebServlet("/api/atm")
public class ATMServlet extends HttpServlet {
    private ATMDao atmDao = new ATMDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
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
        String role = (String) claims.get("role");
        
        res.setContentType("application/json"); 
        PrintWriter out = res.getWriter();
        
        if (!"ADMIN".equals(role)) { 
        	res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        	out.println("{\"error\":\"Access denied. Only Admin can view ATM balance.\"}");
        	return; 
        	}
        ATM atm = atmDao.getATMById(1);

        res.setContentType("application/json");
        PrintWriter pw = res.getWriter();
        pw.println("{\"atmBalance\":" + atm.getTotalBalance() + "}");

        if (atm.getTotalBalance() < 10000) {
            pw.println("{\"warning\":\"ATM balance is low (<10k). Admin must add funds.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
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
        String role = (String) claims.get("role");

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        if (!"ADMIN".equals(role)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("{\"error\":\"Access denied. Only Admin can add ATM funds.\"}");
            return;
        }

        double amount = Double.parseDouble(req.getParameter("amount"));
        atmDao.addFunds(1, amount);

        ATM atm = atmDao.getATMById(1);
        out.println("{\"status\":\"success\",\"newAtmBalance\":" + atm.getTotalBalance() + "}");
    }
}
