package com.atm.controller;

import com.atm.controller.AdminServlet;
import com.atm.service.EmailService;
import com.atm.util.DBUtil;
import com.atm.util.TokenUtil;
import com.atm.service.RegexService;
import com.atm.util.HashUtil;
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
class AdminServletTest {
    private AdminServlet servlet;
    private EmailService emailServiceMock;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private StringWriter responseWriter;
    @BeforeEach
    void setUp() throws Exception {
        emailServiceMock = mock(EmailService.class);
        servlet = new AdminServlet(emailServiceMock);
        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(responseMock.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }
    @Test
    void testDoGetForbiddenRole() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer token");
        Claims claims = mock(Claims.class);
        when(claims.get("role")).thenReturn("USER");
        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            tokenMock.when(() -> TokenUtil.getClaims("token")).thenReturn(claims);
            servlet.doGet(requestMock, responseMock);
            String output = responseWriter.toString();
            assertTrue(output.contains("Access denied"));
            verify(responseMock).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }
    @Test
    void testDoGetInvalidPath() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer token");
        when(requestMock.getParameter("path")).thenReturn("invalid");
        Claims claims = mock(Claims.class);
        when(claims.get("role")).thenReturn("ADMIN");
        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
             MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            tokenMock.when(() -> TokenUtil.getClaims("token")).thenReturn(claims);
            Connection connMock = mock(Connection.class);
            dbMock.when(DBUtil::getConnection).thenReturn(connMock);
            servlet.doGet(requestMock, responseMock);
            String output = responseWriter.toString();
            assertTrue(output.contains("Invalid Path"));
        }
    }
    @Test
    void testDoPostInvalidPhone() throws Exception {
        when(requestMock.getParameter("path")).thenReturn("insert");
        when(requestMock.getParameter("username")).thenReturn("newuser");
        when(requestMock.getParameter("pin")).thenReturn("1234");
        when(requestMock.getParameter("role")).thenReturn("USER");
        when(requestMock.getParameter("phone_number")).thenReturn("badphone");
        when(requestMock.getParameter("email")).thenReturn("user@example.com");
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class);
             MockedStatic<RegexService> regexMock = mockStatic(RegexService.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(mock(Connection.class));
            regexMock.when(() -> RegexService.isValidPhone("badphone")).thenReturn(false);
            servlet.doPost(requestMock, responseMock);
            String output = responseWriter.toString();
            assertTrue(output.contains("Invalid phone number"));
            verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    @Test
    void testDoPostInvalidEmail() throws Exception {
        when(requestMock.getParameter("path")).thenReturn("insert");
        when(requestMock.getParameter("username")).thenReturn("newuser");
        when(requestMock.getParameter("pin")).thenReturn("1234");
        when(requestMock.getParameter("role")).thenReturn("USER");
        when(requestMock.getParameter("phone_number")).thenReturn("9876543210");
        when(requestMock.getParameter("email")).thenReturn("bademail");
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class);
             MockedStatic<RegexService> regexMock = mockStatic(RegexService.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(mock(Connection.class));
            regexMock.when(() -> RegexService.isValidPhone("9876543210")).thenReturn(true);
            regexMock.when(() -> RegexService.isValidEmail("bademail")).thenReturn(false);
            servlet.doPost(requestMock, responseMock);
            String output = responseWriter.toString();
            assertTrue(output.contains("Invalid email address"));
            verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    @Test
    void testDoPostSuccess() throws Exception {
        when(requestMock.getParameter("path")).thenReturn("insert");
        when(requestMock.getParameter("username")).thenReturn("newuser");
        when(requestMock.getParameter("pin")).thenReturn("1234");
        when(requestMock.getParameter("role")).thenReturn("USER");
        when(requestMock.getParameter("phone_number")).thenReturn("9876543210");
        when(requestMock.getParameter("email")).thenReturn("user@example.com");
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class);
             MockedStatic<RegexService> regexMock = mockStatic(RegexService.class);
             MockedStatic<HashUtil> hashMock = mockStatic(HashUtil.class)) {
            Connection connMock = mock(Connection.class);
            PreparedStatement psMock = mock(PreparedStatement.class);
            when(connMock.prepareStatement(anyString())).thenReturn(psMock);
            dbMock.when(DBUtil::getConnection).thenReturn(connMock);
            regexMock.when(() -> RegexService.isValidPhone("9876543210")).thenReturn(true);
            regexMock.when(() -> RegexService.isValidEmail("user@example.com")).thenReturn(true);
            regexMock.when(() -> RegexService.normalizePhone("9876543210")).thenReturn("9876543210");
            hashMock.when(() -> HashUtil.hashPassword("1234")).thenReturn("hashedPin");
            servlet.doPost(requestMock, responseMock);
            String output = responseWriter.toString();
            assertTrue(output.contains("User created successfully"));
            verify(emailServiceMock).sendEmail(eq("user@example.com"), contains("Welcome"), anyString());
        }
    }
    
   @Test
	void testUsersPath() throws Exception {
	    when(requestMock.getHeader("Authorization")).thenReturn("Bearer token");
	    when(requestMock.getParameter("path")).thenReturn("users");
	
	    Claims claims = mock(Claims.class);
	    when(claims.get("role")).thenReturn("ADMIN");
	
	    try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
	         MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
	
	        tokenMock.when(() -> TokenUtil.getClaims("token")).thenReturn(claims);
	
	        Connection connMock = mock(Connection.class);
	        PreparedStatement psMock = mock(PreparedStatement.class);
	        ResultSet rsMock = mock(ResultSet.class);
	
	        when(connMock.prepareStatement(anyString())).thenReturn(psMock);
	        when(psMock.executeQuery()).thenReturn(rsMock);
	
	        when(rsMock.next()).thenReturn(true, false);
	        when(rsMock.getInt("id")).thenReturn(1);
	        when(rsMock.getString("username")).thenReturn("john");
	        when(rsMock.getString("role")).thenReturn("USER");
	        when(rsMock.getDouble("balance")).thenReturn(1000.0);
	        when(rsMock.getString("phone_number")).thenReturn("9876543210");
	        when(rsMock.getString("email")).thenReturn("john@example.com");
	
	        dbMock.when(DBUtil::getConnection).thenReturn(connMock);
	
	        servlet.doGet(requestMock, responseMock);
	
	        String output = responseWriter.toString();
	        assertTrue(output.contains("john"));
	        assertTrue(output.contains("1000.0"));
	    }
	}
  
    @Test
    void testTransactionsPath() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer token");
        when(requestMock.getParameter("path")).thenReturn("transactions");
        Claims claims = mock(Claims.class);
        when(claims.get("role")).thenReturn("ADMIN");
        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
             MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            tokenMock.when(() -> TokenUtil.getClaims("token")).thenReturn(claims);
            Connection connMock = mock(Connection.class);
            PreparedStatement psMock = mock(PreparedStatement.class);
            ResultSet rsMock = mock(ResultSet.class);
            when(connMock.prepareStatement(anyString())).thenReturn(psMock);
            when(psMock.executeQuery()).thenReturn(rsMock);
            when(rsMock.next()).thenReturn(true, false);
            when(rsMock.getInt("id")).thenReturn(10);
            when(rsMock.getString("username")).thenReturn("john");
            when(rsMock.getString("type")).thenReturn("deposit");
            when(rsMock.getDouble("amount")).thenReturn(500.0);
            when(rsMock.getTimestamp("timestamp"))
                .thenReturn(new java.sql.Timestamp(System.currentTimeMillis()));
            dbMock.when(DBUtil::getConnection).thenReturn(connMock);
            servlet.doGet(requestMock, responseMock);
            String output = responseWriter.toString();
            assertTrue(output.contains("deposit"));
            assertTrue(output.contains("500.0"));
        }
    }
    
    
    
    @Test
    void testAtmPathLowBalanceTriggersEmail() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer token"); 
        when(requestMock.getParameter("path")).thenReturn("atm");
        Claims claims = mock(Claims.class);
        when(claims.get("role")).thenReturn("ADMIN");
        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
             MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            tokenMock.when(() -> TokenUtil.getClaims("token")).thenReturn(claims);
            Connection connMock = mock(Connection.class);
            PreparedStatement psMock = mock(PreparedStatement.class);
            ResultSet rsMock = mock(ResultSet.class);
            when(connMock.prepareStatement(anyString())).thenReturn(psMock);
            when(psMock.executeQuery()).thenReturn(rsMock);
            when(rsMock.next()).thenReturn(true);
            when(rsMock.getDouble("balance")).thenReturn(4000.0);
            dbMock.when(DBUtil::getConnection).thenReturn(connMock);
            servlet.doGet(requestMock, responseMock);
            String output = responseWriter.toString();
            assertTrue(output.contains("4000.0"));
            verify(emailServiceMock).sendEmail(
                eq("aswin.c201@gmail.com"),
                contains("ATM Low Balance Alert"),
                anyString()
            );
        }
    }
}