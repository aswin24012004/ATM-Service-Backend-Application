package com.atm.controller;
import com.atm.dao.ATMDao;
import com.atm.dao.UserDao;
import com.atm.model.ATM;
import com.atm.model.User;
import com.atm.service.EmailService;
import com.atm.util.TokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import java.io.PrintWriter;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WithdrawServletTest {
    private WithdrawServlet servlet;
    private UserDao userDaoMock;
    private ATMDao atmDaoMock;
    private EmailService emailServiceMock;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private StringWriter responseWriter;
    @BeforeEach
    void setUp() throws Exception {
        userDaoMock = mock(UserDao.class);
        atmDaoMock = mock(ATMDao.class);
        emailServiceMock = mock(EmailService.class);
        servlet = new WithdrawServlet(userDaoMock, atmDaoMock, emailServiceMock);
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
    void testATMInsufficientCash() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer goodToken");
        when(requestMock.getParameter("amount")).thenReturn("20000"); 
        User user = new User(1, "john", "hashedPin", "USER", 50000.0, null, null);
        user.setEmail("john@gmail.com");
        ATM atm = new ATM(1, 15000.0);
        atm.setTotalBalance(15000.0); 
        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn("john");
            tokenMock.when(() -> TokenUtil.validateToken("goodToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("goodToken")).thenReturn(claims);
            when(userDaoMock.findByUsername("john")).thenReturn(user);
            when(atmDaoMock.getATMById(1)).thenReturn(atm);
            servlet.doPost(requestMock, responseMock);
            String output = responseWriter.toString();
            assertTrue(output.contains("ATM has insufficient cash"));
            verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    @Test
    void testUserInsufficientFunds() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer goodToken");
        when(requestMock.getParameter("amount")).thenReturn("2000");
        User user = new User(1, "john", "hashedPin", "USER", 1000.0, null, null);
    
        user.setUsername("john");
        user.setBalance(1000.0);
        user.setEmail("john@gmail.com");
        ATM atm = new ATM(1,20000);
        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn("john");
            tokenMock.when(() -> TokenUtil.validateToken("goodToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("goodToken")).thenReturn(claims);
            when(userDaoMock.findByUsername("john")).thenReturn(user);
            when(atmDaoMock.getATMById(1)).thenReturn(atm);
            servlet.doPost(requestMock, responseMock);
            String output = responseWriter.toString();
            assertTrue(output.contains("Insufficient user funds"));
            verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    @Test
    void testSuccessfulWithdrawal() throws Exception {
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer goodToken");
        when(requestMock.getParameter("amount")).thenReturn("2000");
        
        User user = new User(1, "john", "hashedPin", "USER", 1000.0, null, null);
        
        user.setUsername("john");
        user.setBalance(5000.0);
        user.setEmail("john@gmail.com");
        ATM atm = new ATM(1,20000);
        User updatedUser = new User(1, "john", "hashedPin", "USER", 1000.0, null, null);
        updatedUser.setUsername("john");
        updatedUser.setBalance(3000.0);
        updatedUser.setEmail("john@gmail.com");
        ATM updatedAtm = new ATM(1,18000);
        try (MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn("john");
            tokenMock.when(() -> TokenUtil.validateToken("goodToken")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.getClaims("goodToken")).thenReturn(claims);
            when(userDaoMock.findByUsername("john")).thenReturn(user, updatedUser);
            when(atmDaoMock.getATMById(1)).thenReturn(atm, updatedAtm);
            servlet.doPost(requestMock, responseMock);
            String output = responseWriter.toString();
            assertTrue(output.contains("\"status\":\"success\""));
            assertTrue(output.contains("newUserBalance"));
            assertTrue(output.contains("newAtmBalance"));
            verify(userDaoMock).updateBalance("john", 3000.0);
            verify(atmDaoMock).checkBalance(1, 2000.0);
            verify(userDaoMock).logTransaction("john", "withdraw", 2000.0);
            verify(emailServiceMock).sendEmail(eq("john@gmail.com"), contains("Withdrawal Alert"), anyString());
        }
    }
}