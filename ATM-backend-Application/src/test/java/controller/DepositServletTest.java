package controller;

import com.atm.controller.DepositServlet;
import com.atm.service.EmailService;
import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepositServletTest {

    private DepositServlet servlet;
    private EmailService emailServiceMock;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        emailServiceMock = mock(EmailService.class);
        servlet = new DepositServlet(emailServiceMock);

        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(responseMock.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testMissingAuthHeader() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn(null);

        servlet.doPost(requestMock, responseMock);

        String output = responseWriter.toString();
        assertTrue(output.contains("Missing or invalid Authorization header"));
        verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testInvalidToken() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer badToken");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.validateToken("badToken")).thenReturn(false);

            servlet.doPost(requestMock, responseMock);

            String output = responseWriter.toString();
            assertTrue(output.contains("Invalid token"));
            verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Test
    void testSuccessfulDeposit() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer goodToken");
        when(requestMock.getParameter("amount")).thenReturn("1000");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("john");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
             MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {

            tokenMock.when(() -> TokenUtil.validateToken("goodToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("goodToken")).thenReturn(claims);

            Connection connMock = mock(Connection.class);
            PreparedStatement psMock = mock(PreparedStatement.class);
            ResultSet rsMock = mock(ResultSet.class);

            when(connMock.prepareStatement(anyString())).thenReturn(psMock);
            when(psMock.executeQuery()).thenReturn(rsMock);
            when(rsMock.next()).thenReturn(true);
            when(rsMock.getDouble("balance")).thenReturn(2000.0);
            when(rsMock.getString("email")).thenReturn("john@example.com");

            dbMock.when(DBUtil::getConnection).thenReturn(connMock);

            servlet.doPost(requestMock, responseMock);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"status\":\"success\""));
            assertTrue(output.contains("newBalance"));
            verify(emailServiceMock).sendEmail(eq("john@example.com"), contains("Deposit Alert"), anyString());
        }
    }
}
