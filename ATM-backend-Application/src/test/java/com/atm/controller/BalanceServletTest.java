package com.atm.controller;
import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BalanceServletTest {
    private BalanceServlet servlet;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private HttpSession sessionMock;
    private StringWriter responseWriter;
    @BeforeEach
    void setUp() throws Exception {
        servlet = new BalanceServlet();
        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        sessionMock = mock(HttpSession.class);
        responseWriter = new StringWriter();
        when(responseMock.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }
    @Test
    void testDoGetWithSession() throws Exception {
        when(requestMock.getSession(false)).thenReturn(sessionMock);
        when(sessionMock.getAttribute("username")).thenReturn("aswin");
        // Mock DBUtil connection and result
        Connection connMock = mock(Connection.class);
        PreparedStatement psMock = mock(PreparedStatement.class);
        ResultSet rsMock = mock(ResultSet.class);
        when(connMock.prepareStatement(anyString())).thenReturn(psMock);
        when(psMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getDouble("balance")).thenReturn(500.0);
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(connMock);
            servlet.doGet(requestMock, responseMock);
            String output = responseWriter.toString();
            assertTrue(output.contains("\"username\":\"aswin\""));
            assertTrue(output.contains("\"balance\":500.0"));
        }
    }
    @Test
    void testDoGetWithToken() throws Exception {
        when(requestMock.getSession(false)).thenReturn(null);
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer testToken");
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("john");
        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.getClaims("testToken")).thenReturn(claims);
            Connection connMock = mock(Connection.class);
            PreparedStatement psMock = mock(PreparedStatement.class);
            ResultSet rsMock = mock(ResultSet.class);
            when(connMock.prepareStatement(anyString())).thenReturn(psMock);
            when(psMock.executeQuery()).thenReturn(rsMock);
            when(rsMock.next()).thenReturn(true);
            when(rsMock.getDouble("balance")).thenReturn(1000.0);
            try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
                dbMock.when(DBUtil::getConnection).thenReturn(connMock);
                servlet.doGet(requestMock, responseMock);
                String output = responseWriter.toString();
                assertTrue(output.contains("\"username\":\"john\""));
                assertTrue(output.contains("\"balance\":1000.0"));
            }
        }
    }
//    @Test
//    void testDoGetUserNotFound() throws Exception {
//        when(requestMock.getSession(false)).thenReturn(sessionMock);
//        when(sessionMock.getAttribute("username")).thenReturn("ghost");
//        Connection connMock = mock(Connection.class);
//        PreparedStatement psMock = mock(PreparedStatement.class);
//        ResultSet rsMock = mock(ResultSet.class);
//        when(connMock.prepareStatement(anyString())).thenReturn(psMock);
//        when(psMock.executeQuery()).thenReturn(rsMock);
//        when(rsMock.next()).thenReturn(false);
//        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
//            dbMock.when(DBUtil::getConnection).thenReturn(connMock);
//            servlet.doGet(requestMock, responseMock);
//            String output = responseWriter.toString();
//            assertTrue(output.contains("User not found"));
//            verify(responseMock).setStatus(HttpServletResponse.SC_NOT_FOUND);
//        }
//    }
}
