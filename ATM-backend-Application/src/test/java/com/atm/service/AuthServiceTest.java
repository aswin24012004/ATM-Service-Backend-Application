package com.atm.service;

import com.atm.dao.UserDao;
import com.atm.model.User;
import com.atm.util.HashUtil;
import com.atm.util.TokenUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AuthServiceTest {
    private UserDao userDaoMock;
    private User testUser;
    @BeforeEach
    void setUp() {
        userDaoMock = mock(UserDao.class);
        testUser = new User(1, "aswin", "hashedPin", "USER", 100.0, null, null);
        testUser.setPhoneNumber("9876543210");
        testUser.setEmail("aswin@example.com");
        try {
        	java.lang.reflect.Field field = AuthService.class.getDeclaredField("userDao");
            field.setAccessible(true);
            field.set(null, userDaoMock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void testLoginSuccess() {
        when(userDaoMock.findByUsername("aswin")).thenReturn(testUser);
        try (MockedStatic<HashUtil> hashMock = mockStatic(HashUtil.class);
             MockedStatic<TokenUtil> tokenMock = mockStatic(TokenUtil.class)) {
            hashMock.when(() -> HashUtil.checkPassword("1234", "hashedPin")).thenReturn(true);
            tokenMock.when(() -> TokenUtil.generateToken("aswin", "USER")).thenReturn("mockToken");
            String token = AuthService.login("aswin", "1234");
            assertNotNull(token);
            assertEquals("mockToken", token);
        }
    }
    @Test
    void testLoginFailureWrongPassword() {
        when(userDaoMock.findByUsername("aswin")).thenReturn(testUser);
        try (MockedStatic<HashUtil> hashMock = mockStatic(HashUtil.class)) {
            hashMock.when(() -> HashUtil.checkPassword("wrong", "hashedPin")).thenReturn(false);
            String token = AuthService.login("aswin", "wrong");
            assertNull(token);
        }
    }
    @Test
    void testLoginFailureNoUser() {
        when(userDaoMock.findByUsername("ghost")).thenReturn(null);
        String token = AuthService.login("ghost", "1234");
        assertNull(token);
    }
    @Test
    void testGetRoleDelegatesToDao() {
        when(userDaoMock.getRole("aswin")).thenReturn("ADMIN");
        AuthService authService = new AuthService();
        assertEquals("ADMIN", authService.getRole("aswin"));
    }
    @Test
    void testGetEmailDelegatesToDao() {
        when(userDaoMock.getEmail("aswin")).thenReturn("aswin.c201@gmail.com");
        AuthService authService = new AuthService();
        assertEquals("aswin.c201@gmail.com", authService.getEmail("aswin"));
    }
}
