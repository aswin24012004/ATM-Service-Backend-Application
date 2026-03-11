package com.atm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

import com.atm.service.AuthService;
import com.atm.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
//    private AuthService authService = new AuthService();
//    private EmailService emailService = new EmailService();
    
    protected AuthService authService;
    protected EmailService emailService;

    public LoginServlet() {
        this(new AuthService(), new EmailService());
    }

    public LoginServlet(AuthService authService, EmailService emailService) {
        this.authService = authService;
        this.emailService = emailService;
    }   
    @Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = req.getParameter("username");
        String pin = req.getParameter("pin");

        logger.info("Login attempt: username={}", username);

        String token = AuthService.login(username, pin);

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        if (token != null) {
            // Create HttpSession
            HttpSession session = req.getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("role", getAuthService().getRole(username));

            out.println("{\"status\":\"success\",\"token\":\"" + token + "\"}");
            logger.info("Login successful: username={}", username);

            // Send welcome email
            try {
                String email = getAuthService().getEmail(username); //
                getEmailService().sendEmail(email, "Welcome Back to ATM System",
                    "Dear " + username + ",\nYou have successfully logged in to your ATM account.");
            } catch (Exception e) {
                logger.error("Failed to send login email to user={}", username, e);
            }

        } else {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.println("{\"status\":\"error\",\"message\":\"Invalid credentials\"}");
            logger.warn("Login failed: username={}", username);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.getWriter().println("{\"status\":\"success\",\"token\":\"test-token\"}");
        logger.debug("LoginServlet GET called for test token");
    }

	public AuthService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthService authService) {
		this.authService = authService;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
}
