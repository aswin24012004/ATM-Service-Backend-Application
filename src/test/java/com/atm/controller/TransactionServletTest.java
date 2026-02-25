package com.atm.controller;

import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TransactionServletTest {

    private TransactionServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new TransactionServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testDoGetMissingAuthHeader() throws Exception {
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
    void testDoGetAdminRoleFetchesAllTransactions() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer goodToken");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin");
        when(claims.get("role")).thenReturn("ADMIN");

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(Collections.emptyList());

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
             MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {

            tokenMock.when(() -> TokenUtil.validateToken("goodToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("goodToken")).thenReturn(claims);
            dbMock.when(DBUtil::getJdbcTemplate).thenReturn(jdbcTemplate);

            servlet.doGet(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("[]")); // empty list JSON
        }
    }

    @Test
    void testDoGetUserRoleFetchesOwnTransactions() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer userToken");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("alice");
        when(claims.get("role")).thenReturn("CUSTOMER");

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), eq("alice"))).thenReturn(Collections.emptyList());

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
             MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {

            tokenMock.when(() -> TokenUtil.validateToken("userToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("userToken")).thenReturn(claims);
            dbMock.when(DBUtil::getJdbcTemplate).thenReturn(jdbcTemplate);

            servlet.doGet(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("[]"));
        }
    }
}
