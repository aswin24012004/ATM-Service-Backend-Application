package com.atm.controller;

import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BalanceServletTest {

    private BalanceServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new BalanceServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testDoGetWithSession() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("username")).thenReturn("alice");
        when(request.getSession(false)).thenReturn(session);

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(Double.class)))
                .thenReturn(1000.0);

        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getJdbcTemplate).thenReturn(jdbcTemplate);

            servlet.doGet(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"username\":\"alice\""));
            assertTrue(output.contains("\"balance\":1000.0"));
        }
    }

    @Test
    void testDoGetWithToken() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer goodToken");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("bob");

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(Double.class)))
                .thenReturn(500.0);

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
             MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {

            tokenMock.when(() -> TokenUtil.getClaims("goodToken")).thenReturn(claims);
            dbMock.when(DBUtil::getJdbcTemplate).thenReturn(jdbcTemplate);

            servlet.doGet(request, response);

            String output = responseWriter.toString();
            assertTrue(output.contains("\"username\":\"bob\""));
            assertTrue(output.contains("\"balance\":500.0"));
        }
    }
}
