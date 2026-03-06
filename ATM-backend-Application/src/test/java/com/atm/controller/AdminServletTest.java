package com.atm.controller;

import com.atm.dao.UserDao;
import com.atm.dao.TransactionDao;
import com.atm.dao.ATMDao;
import com.atm.util.TokenUtil;
import com.atm.service.EmailService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServletTest {

    private AdminServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AdminServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }



    @Test
    void testDoGetUnauthorizedRole() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJBZG1pbiIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTc3MjAxNDM2MSwiZXhwIjoxNzcyMDE3OTYxfQ.R56bT8vW1CI4Wm8TKPhSavZunglOY8wLzCZoJMw9Yu8");

        Claims claims = mock(Claims.class);
        when(claims.get("role")).thenReturn("USER");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(
            		() -> TokenUtil
            		.getClaims("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJBZG1pbiIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTc3MjAxNDM2MSwiZXhwIjoxNzcyMDE3OTYxfQ.R56bT8vW1CI4Wm8TKPhSavZunglOY8wLzCZoJMw9Yu8"))
            		.thenReturn(claims);

            servlet.doGet(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("Access denied"));
        }
    }


    @Test
    void testDoGetUsersPath() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer adminToken");
        when(request.getParameter("path")).thenReturn("users");

        Claims claims = mock(Claims.class);
        when(claims.get("role")).thenReturn("ADMIN");

        UserDao userDao = mock(UserDao.class);
        when(userDao.getAllUsers()).thenReturn(Collections.emptyList());

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.getClaims("adminToken")).thenReturn(claims);

            servlet = new AdminServlet() { { this.userDao = userDao; } };
            servlet.doGet(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("[]"));
        }
    }

}
