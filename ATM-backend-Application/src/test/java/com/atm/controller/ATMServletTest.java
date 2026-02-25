package com.atm.controller;

import com.atm.dao.ATMDao;
import com.atm.model.ATM;
import com.atm.util.TokenUtil;
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

class ATMServletTest {

    private ATMServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ATMServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testDoGetUnauthorizedHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        servlet.doGet(request, response);

        String output = responseWriter.toString();
        assertTrue(output.contains("Missing or invalid Authorization header"));
    }

    @Test
    void testDoGetInvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer badToken");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.validateToken("badToken")).thenReturn(false);

            servlet.doGet(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("Invalid token"));
        }
    }

    @Test
    void testDoGetAdminRoleShowsBalance() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer goodToken");

        Claims claims = mock(Claims.class);
        when(claims.get("role")).thenReturn("ADMIN");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.validateToken("goodToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("goodToken")).thenReturn(claims);

            // Mock ATMDao
            ATMDao atmDaoMock = mock(ATMDao.class);
            ATM atm = new ATM();
            atm.setId(1);
            atm.setTotalBalance(15000.0);
            when(atmDaoMock.getATMById(1)).thenReturn(atm);

            // Inject mock ATMDao into servlet
            servlet = new ATMServlet() {
                { this.atmDao = atmDaoMock; }
            };

            servlet.doGet(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"atmBalance\":15000.0"));
        }
    }
}
