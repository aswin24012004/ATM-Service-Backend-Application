package controller;

import com.atm.controller.LoginServlet;
import com.atm.service.AuthService;
import com.atm.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServletTest {

    private LoginServlet servlet;
    private AuthService authServiceMock;
    private EmailService emailServiceMock;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private HttpSession sessionMock;
    private StringWriter responseWriter;

    @BeforeEach
		void setUp() throws Exception {
		    authServiceMock = mock(AuthService.class);
		    emailServiceMock = mock(EmailService.class);
		    servlet = new LoginServlet(authServiceMock, emailServiceMock);
		
		    requestMock = mock(HttpServletRequest.class);
		    responseMock = mock(HttpServletResponse.class);
		    sessionMock = mock(HttpSession.class);
		
		    responseWriter = new StringWriter();
		    when(responseMock.getWriter()).thenReturn(new PrintWriter(responseWriter));
		}


    @Test
    void testDoPostSuccess() throws Exception {
        when(requestMock.getParameter("username")).thenReturn("aswin");
        when(requestMock.getParameter("pin")).thenReturn("1234");
        when(authServiceMock.getRole("aswin")).thenReturn("USER");
        when(authServiceMock.getEmail("aswin")).thenReturn("aswin@example.com");

        // Static login method returns token
        try (MockedStatic<AuthService> authStatic = mockStatic(AuthService.class)) {
            authStatic.when(() -> AuthService.login("aswin", "1234")).thenReturn("mockToken");

            when(requestMock.getSession(true)).thenReturn(sessionMock);

            servlet.doPost(requestMock, responseMock);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"status\":\"success\""));
            assertTrue(output.contains("\"token\":\"mockToken\""));

            verify(sessionMock).setAttribute("username", "aswin");
            verify(sessionMock).setAttribute("role", "USER");
            verify(emailServiceMock).sendEmail(eq("aswin@example.com"), anyString(), contains("Dear aswin"));
        }
    }

    @Test
    void testDoPostFailure() throws Exception {
        when(requestMock.getParameter("username")).thenReturn("ghost");
        when(requestMock.getParameter("pin")).thenReturn("wrong");

        try (MockedStatic<AuthService> authStatic = mockStatic(AuthService.class)) {
            authStatic.when(() -> AuthService.login("ghost", "wrong")).thenReturn(null);

            servlet.doPost(requestMock, responseMock);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"status\":\"error\""));
            assertTrue(output.contains("Invalid credentials"));

            verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Test
    void testDoGet() throws Exception {
        servlet.doGet(requestMock, responseMock);
        String output = responseWriter.toString();
        assertTrue(output.contains("\"status\":\"success\""));
        assertTrue(output.contains("\"token\":\"test-token\""));
    }
}
