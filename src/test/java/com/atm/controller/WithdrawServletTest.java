package com.atm.controller;

import com.atm.dao.ATMDao;
import com.atm.dao.UserDao;
import com.atm.model.ATM;
import com.atm.model.User;
import com.atm.util.TokenUtil;
import com.atm.service.EmailService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class WithdrawServletTest {

    private WithdrawServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = mock(WithdrawServlet.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testUnauthorizedHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        servlet.doPost(request, response);

        String output = responseWriter.toString();
        assertTrue(output.contains("Missing or invalid Authorization header"));
    }

    @Test
    void testInvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer badToken");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.validateToken("badToken")).thenReturn(false);

            servlet.doPost(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("Invalid token"));
        }
    }

    @Test
    void testSuccessfulWithdrawal() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer goodToken");
        when(request.getParameter("amount")).thenReturn("500");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("alice");

        User user = new User();
        user.setUsername("alice");
        user.setBalance(2000.0);
        user.setEmail("alice@example.com");

        ATM atm = new ATM();
        atm.setId(1);
        atm.setTotalBalance(20000.0);

        UserDao userDaoMock = mock(UserDao.class);
        ATMDao atmDaoMock = mock(ATMDao.class);
        EmailService emailServiceMock = mock(EmailService.class);

        when(userDaoMock.findByUsername("alice")).thenReturn(user);
        when(atmDaoMock.getATMById(1)).thenReturn(atm);

        // Updated balances after withdrawal
        User updatedUser = new User();
        updatedUser.setUsername("alice");
        updatedUser.setBalance(1500.0);
        updatedUser.setEmail("alice@example.com");

        ATM updatedAtm = new ATM();
        updatedAtm.setId(1);
        updatedAtm.setTotalBalance(19500.0);

        when(userDaoMock.findByUsername("alice")).thenReturn(updatedUser);
        when(atmDaoMock.getATMById(1)).thenReturn(updatedAtm);

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.validateToken("goodToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("goodToken")).thenReturn(claims);

            servlet = new WithdrawServlet() {
                { this.userDao = userDaoMock; 
                this.atmDao = atmDaoMock; 
                this.emailService = emailServiceMock; }
            };

            servlet.doPost(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"status\":\"success\""));
            assertTrue(output.contains("\"newUserBalance\":1500.0"));
            assertTrue(output.contains("\"newAtmBalance\":19500.0"));
        }
    }
}
