package com.atm.controller;

import com.atm.dao.TransactionDao;
import com.atm.model.ATM;
import com.atm.model.User;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TransactionServletTest {

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
    void testDoGetMissingAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        TransactionServlet servlet = new TransactionServlet(new TransactionDao());
        servlet.doGet(request, response);

        String output = responseWriter.toString();
        assertTrue(output.contains("Missing or invalid Authorization header"));
    }

    @Test
    void testDoGetInvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer badToken");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.validateToken("badToken")).thenReturn(false);

            TransactionServlet servlet = new TransactionServlet(new TransactionDao());
            servlet.doGet(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("Invalid token"));
        }
    }

    @Test
    void testDoGetAdminRoleFetchesAllTransactions() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer goodToken");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin");
        when(claims.get("role")).thenReturn("ADMIN");

        TransactionDao txDao = mock(TransactionDao.class);
        when(txDao.findAll()).thenReturn(Collections.emptyList());

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.validateToken("goodToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("goodToken")).thenReturn(claims);

            TransactionServlet servlet = new TransactionServlet(txDao);
            servlet.doGet(request, response);

            String output = responseWriter.toString().trim();
            assertTrue(output.contains("[]"));
            verify(txDao).findAll();
        }
    }

    @Test
    void testDoGetUserRoleFetchesOwnTransactions() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer userToken");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("alice");
        when(claims.get("role")).thenReturn("CUSTOMER");

        TransactionDao txDao = mock(TransactionDao.class);
        when(txDao.findByUsername("alice")).thenReturn(Collections.emptyList());

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.validateToken("userToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("userToken")).thenReturn(claims);

            TransactionServlet servlet = new TransactionServlet(txDao);
            servlet.doGet(request, response);

            String output = responseWriter.toString().trim();
            assertTrue(output.contains("[]"));
            verify(txDao).findByUsername("alice");
        }
    }
}
