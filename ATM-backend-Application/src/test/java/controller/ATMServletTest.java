package controller;

import com.atm.controller.ATMServlet;
import com.atm.dao.ATMDao;
import com.atm.model.ATM;
import com.atm.service.EmailService;
import com.atm.util.ConfigUtil;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ATMServletTest {

    private ATMServlet servlet;
    private ATMDao atmDaoMock;
    private EmailService emailServiceMock;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        atmDaoMock = mock(ATMDao.class);
        emailServiceMock = mock(EmailService.class);

        servlet = new ATMServlet(atmDaoMock, emailServiceMock);

        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(responseMock.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }


    @Test
    void testDoGetMissingAuthHeader() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn(null);

        servlet.doGet(requestMock, responseMock);

        String output = responseWriter.toString();
        assertTrue(output.contains("Missing or invalid Authorization header"));
        verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testDoGetInvalidToken() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer badToken");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.validateToken("badToken")).thenReturn(false);

            servlet.doGet(requestMock, responseMock);

            String output = responseWriter.toString();
            assertTrue(output.contains("Invalid token"));
            verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Test
    void testDoGetForbiddenRole() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer goodToken");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            Claims claims = mock(Claims.class);
            when(claims.get("role")).thenReturn("USER");
            tokenMock.when(() -> TokenUtil.validateToken("goodToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("goodToken")).thenReturn(claims);

            servlet.doGet(requestMock, responseMock);

            String output = responseWriter.toString();
            assertTrue(output.contains("Access denied"));
            verify(responseMock).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Test
    void testDoGetAdminLowBalanceTriggersEmail() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer adminToken");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
             MockedStatic<ConfigUtil> configMock = mockStatic(ConfigUtil.class)) {

            Claims claims = mock(Claims.class);
            when(claims.get("role")).thenReturn("ADMIN");
            tokenMock.when(() -> TokenUtil.validateToken("adminToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("adminToken")).thenReturn(claims);

            ATM atm = new ATM(1, 50.0);  // id=1, balance=50.0

            atm.setTotalBalance(5000.0);
            when(atmDaoMock.getATMById(1)).thenReturn(atm);

            configMock.when(() -> ConfigUtil.get("mail.username")).thenReturn("aswin.c2401@gmail.com");

            servlet.doGet(requestMock, responseMock);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"atmBalance\":5000.0"));
            assertTrue(output.contains("warning"));

            verify(emailServiceMock).sendEmail(eq("aswin.c2401@gmail.com"), contains("ATM Low Balance Alert"), anyString());
        }
    }

    @Test
    void testDoPostUnauthorizedRole() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer userToken");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            Claims claims = mock(Claims.class);
            when(claims.get("role")).thenReturn("USER");
            tokenMock.when(() -> TokenUtil.validateToken("userToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("userToken")).thenReturn(claims);

            servlet.doPost(requestMock, responseMock);

            String output = responseWriter.toString();
            assertTrue(output.contains("Access denied"));
            verify(responseMock).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Test
    void testDoPostAdminAddsFunds() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer adminToken");
        when(requestMock.getParameter("amount")).thenReturn("2000");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            Claims claims = mock(Claims.class);
            when(claims.get("role")).thenReturn("ADMIN");
            tokenMock.when(() -> TokenUtil.validateToken("adminToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("adminToken")).thenReturn(claims);

            ATM atm = new ATM(1, 100.0);  
            atm.setTotalBalance(12000.0);
            when(atmDaoMock.getATMById(1)).thenReturn(atm);

            servlet.doPost(requestMock, responseMock);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"status\":\"success\""));
            assertTrue(output.contains("newAtmBalance"));
            verify(atmDaoMock).addAmount(1, 2000.0);
        }
    }
}
