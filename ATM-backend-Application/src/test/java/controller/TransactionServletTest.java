package controller;

import com.atm.controller.TransactionServlet;
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

class TransactionServletTest {

    private TransactionServlet servlet;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new TransactionServlet();
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
    void testDoGetAdminRetrievesTransactions() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer adminToken");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin");
        when(claims.get("role")).thenReturn("ADMIN");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
             MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {

            tokenMock.when(() -> TokenUtil.validateToken("adminToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("adminToken")).thenReturn(claims);

            Connection connMock = mock(Connection.class);
            PreparedStatement psMock = mock(PreparedStatement.class);
            ResultSet rsMock = mock(ResultSet.class);

            when(connMock.prepareStatement(anyString())).thenReturn(psMock);
            when(psMock.executeQuery()).thenReturn(rsMock);
            when(rsMock.next()).thenReturn(true, false);
            when(rsMock.getInt("id")).thenReturn(1);
            when(rsMock.getString("username")).thenReturn("admin");
            when(rsMock.getString("type")).thenReturn("DEPOSIT");
            when(rsMock.getDouble("amount")).thenReturn(1000.0);
            when(rsMock.getTimestamp("timestamp")).thenReturn(new java.sql.Timestamp(System.currentTimeMillis()));

            dbMock.when(DBUtil::getConnection).thenReturn(connMock);

            servlet.doGet(requestMock, responseMock);

            String output = responseWriter.toString();
            assertTrue(output.contains("DEPOSIT"));
            assertTrue(output.contains("admin"));
        }
    }

    @Test
    void testDoGetUserRetrievesTransactions() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer userToken");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("john");
        when(claims.get("role")).thenReturn("USER");

        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class);
             MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {

            tokenMock.when(() -> TokenUtil.validateToken("userToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("userToken")).thenReturn(claims);

            Connection connMock = mock(Connection.class);
            PreparedStatement psMock = mock(PreparedStatement.class);
            ResultSet rsMock = mock(ResultSet.class);

            when(connMock.prepareStatement(anyString())).thenReturn(psMock);
            when(psMock.executeQuery()).thenReturn(rsMock);
            when(rsMock.next()).thenReturn(true, false);
            when(rsMock.getInt("id")).thenReturn(2);
            when(rsMock.getString("username")).thenReturn("john");
            when(rsMock.getString("type")).thenReturn("WITHDRAWAL");
            when(rsMock.getDouble("amount")).thenReturn(200.0);
            when(rsMock.getTimestamp("timestamp")).thenReturn(new java.sql.Timestamp(System.currentTimeMillis()));

            dbMock.when(DBUtil::getConnection).thenReturn(connMock);

            servlet.doGet(requestMock, responseMock);

            String output = responseWriter.toString();
            assertTrue(output.contains("WITHDRAWAL"));
            assertTrue(output.contains("john"));
        }
    }
}
