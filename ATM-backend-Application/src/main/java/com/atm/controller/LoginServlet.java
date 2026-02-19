package com.atm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

import com.atm.service.AuthService;


@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = req.getParameter("username");
        String pin = req.getParameter("pin");
        
        String token = authService.login(username, pin);

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        
        if (token != null) {
            // Create HttpSession
            HttpSession session = req.getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("role", authService.getRole(username));

            out.println("{\"status\":\"success\",\"token\":\"" + token + "\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.println("{\"status\":\"error\",\"message\":\"Invalid credentials\"}");
        }
    }
}

