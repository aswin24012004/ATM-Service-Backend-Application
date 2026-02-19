package com.atm.controller;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.atm.util.DBUtil;
import com.atm.util.HashUtil;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.jdbc.core.JdbcTemplate;
import com.google.gson.Gson;

@WebServlet("/api/admin")

public class AdminServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String token = req.getHeader("Authorization").replace("Bearer ", "");
        Claims claims = TokenUtil.getClaims(token);
        String role = (String) claims.get("role");

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        if (!"ADMIN".equals(role)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("{\"error\":\"Access denied\"}");
            return;
        }

        String path = req.getParameter("path");
        JdbcTemplate jdbc = DBUtil.getJdbcTemplate();

        if ("users".equals(path)) {
            List<Map<String, Object>> users = jdbc.queryForList("SELECT id, username, role, balance FROM users");
            out.println(new Gson().toJson(users));
            
        } else if ("transactions".equals(path)) {
            List<Map<String, Object>> txns = jdbc.queryForList("SELECT * FROM transactions ORDER BY timestamp DESC");
            out.println(new Gson().toJson(txns));
            
        } else {
            out.println("{\"error\":\"Invalid Path\"}");
        }
    }
    
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String path = req.getParameter("path");
        JdbcTemplate jdbc = DBUtil.getJdbcTemplate();
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        
    	 if("insert".equals(path)) {
        	String username = req.getParameter("username"); 
        	String pin = req.getParameter("pin"); 
        	String userRole = req.getParameter("role");
        	String hashedPin = HashUtil.hashPassword(pin);
        	
        	System.out.println(username+", "+pin+", "+userRole);
        	try {
        		jdbc.update("INSERT INTO users(username, pin_hash, role, balance) VALUES(?, ?, ?, ?)", username, hashedPin, userRole, 0.0); 
            	out.println("{\"status\":\"success\",\"message\":\"User created successfully\"}");
        	}
        	catch(Exception e) {
        		System.out.println("Exception: "+e.getMessage());
        		out.println("{\"status\":\"Error\",\"message\":\"User Already Exists\"}");
        	}
        } 
    }
}
