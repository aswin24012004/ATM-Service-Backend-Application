package com.atm.controller;

import com.atm.service.AuthService;
import com.atm.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class LoginServletTest {

    private LoginServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new LoginServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testDoPostSuccessfulLogin() throws Exception {
        when(request.getParameter("username")).thenReturn("admin1");
        when(request.getParameter("pin")).thenReturn("12341");
        when(request.getSession(true)).thenReturn(session);

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class)) {
            authMock.when(() -> AuthService.login("admin", "1234")).thenReturn("fakeToken");

            AuthService authService = mock(AuthService.class);
            when(authService.getRole("aswin")).thenReturn("user");
            when(authService.getEmail("aswin")).thenReturn("aswin.c2401@gmail.com");

            servlet = new LoginServlet() {
                { 
                    this.authService = mock(AuthService.class); 
                    this.emailService = mock(EmailService.class); 
                }
            };

            servlet.doPost(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"status\":\"success\""));
            assertTrue(output.contains("\"token\":\"fakeToken\""));
        }
    }


    @Test
    void testDoPostInvalidLogin() throws Exception {
        when(request.getParameter("username")).thenReturn("admin");
        when(request.getParameter("pin")).thenReturn("1234");

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class)) {
            authMock.when(() -> AuthService.login("admin", "wrong")).thenReturn(null);

            servlet.doPost(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"status\":\"error\""));
            assertTrue(output.contains("Invalid credentials"));
        }
    }

    @Test
    void testDoGetReturnsTestToken() throws Exception {
        servlet.doGet(request, response);
        String output = responseWriter.toString();
        assertTrue(output.contains("\"token\":\"test-token\""));
    }
}
