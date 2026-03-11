package com.atm.filter;

import com.atm.util.TokenUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;


@WebFilter("/api/*")
public class AuthFilter implements Filter {
	public static final int index = 7;
	public void doInit() {
		throw new UnsupportedOperationException();
	}
	
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        
        
        String path = req.getRequestURI();
        // the authentication is not need for the login url
        if (path.endsWith("/api/login")) { 
        	chain.doFilter(request, response); 
        	return; 
        	}

        // Check the HttpSession 
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            chain.doFilter(request, response);
            return;
        }

        // Check the JWT
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(index);
            if (TokenUtil.validateToken(token)) {
                chain.doFilter(request, response);
                return;
            }
        }
        
        HttpServletResponse res = (HttpServletResponse) response;
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().println("{\"error\":\"Unauthorized\"}");
    }
    @Override
    public void destroy() {
    	throw new UnsupportedOperationException();
    }
}
