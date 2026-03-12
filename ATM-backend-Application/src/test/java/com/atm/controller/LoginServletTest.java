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

    private AuthService authService;
    private EmailService emailService;

    @BeforeEach
    void setUp() throws Exception {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        authService = mock(AuthService.class);
        emailService = mock(EmailService.class);

        servlet = new LoginServlet() {
            {
                this.authService = authService;
                this.emailService = emailService;
            }
        };
    }

    @Test
    void testDoPostSuccessfulLogin() throws Exception {
        when(request.getParameter("username")).thenReturn("admin");
        when(request.getParameter("pin")).thenReturn("1234");
        when(request.getSession(true)).thenReturn(session);

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class)) {
            authMock.when(() -> AuthService.login("admin", "1234")).thenReturn("fakeToken");

            when(authService.getRole("admin")).thenReturn("ADMIN");
            when(authService.getEmail("admin")).thenReturn("aswin.c2401@gmail.com");

            servlet.doPost(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"status\":\"success\""));
            assertTrue(output.contains("\"token\":\"fakeToken\""));
        }
    }

    @Test
    void testDoPostInvalidLogin() throws Exception {
        when(request.getParameter("username")).thenReturn("admin");
        when(request.getParameter("pin")).thenReturn("12");

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class)) {
            authMock.when(() -> AuthService.login("admin", "12")).thenReturn(null);

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
