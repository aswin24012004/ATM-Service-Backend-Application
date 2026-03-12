package com.atm.controller;

import com.atm.service.EmailService;
import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DepositServletTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testMissingAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        DepositServlet servlet = new DepositServlet(mock(EmailService.class));
        servlet.doPost(request, response);

        String output = responseWriter.toString();
        assertTrue(output.contains("Missing or invalid Authorization header"));
    }

    @Test
    void testInvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer badToken");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.validateToken("badToken")).thenReturn(false);

            DepositServlet servlet = new DepositServlet(mock(EmailService.class));
            servlet.doPost(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("Invalid token"));
        }
    }

    @Test
    void testSuccessfulDeposit() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer goodToken");
        when(request.getParameter("amount")).thenReturn("100");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("alice");

        // Mock DB connection and statements
        Connection conn = mock(Connection.class);
        PreparedStatement updatePs = mock(PreparedStatement.class);
        PreparedStatement insertPs = mock(PreparedStatement.class);
        PreparedStatement readPs = mock(PreparedStatement.class);
        PreparedStatement emailPs = mock(PreparedStatement.class);
        ResultSet readRs = mock(ResultSet.class);
        ResultSet emailRs = mock(ResultSet.class);

        when(conn.prepareStatement("UPDATE users SET balance = balance + ? WHERE username=?")).thenReturn(updatePs);
        when(conn.prepareStatement("INSERT INTO transactions(username, type, amount) VALUES(?, ?, ?)")).thenReturn(insertPs);
        when(conn.prepareStatement("SELECT balance FROM users WHERE username=?")).thenReturn(readPs);
        when(conn.prepareStatement("SELECT email FROM users WHERE username=?")).thenReturn(emailPs);

        when(readPs.executeQuery()).thenReturn(readRs);
        when(readRs.next()).thenReturn(true);
        when(readRs.getDouble("balance")).thenReturn(1100.0);

        when(emailPs.executeQuery()).thenReturn(emailRs);
        when(emailRs.next()).thenReturn(true);
        when(emailRs.getString("email")).thenReturn("alice@example.com");

        EmailService emailService = mock(EmailService.class);

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
             MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {

            tokenMock.when(() -> TokenUtil.validateToken("goodToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("goodToken")).thenReturn(claims);
            dbMock.when(DBUtil::getConnection).thenReturn(conn);

            DepositServlet servlet = new DepositServlet(emailService);
            servlet.doPost(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"status\":\"success\""));
            assertTrue(output.contains("newBalance"));
            verify(emailService).sendEmail(eq("alice@example.com"), contains("Deposit Alert"), contains("You deposited Rs.100"));
        }
    }
}
