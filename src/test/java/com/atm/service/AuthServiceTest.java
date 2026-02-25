package com.atm.service;

import com.atm.dao.UserDao;
import com.atm.model.User;
import com.atm.util.HashUtil;
import com.atm.util.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        User user = new User();
        user.setUsername("admin");
        user.setRole("ADMIN");
        user.setPinHash(HashUtil.hashPassword("1234"));

        when(userDao.findByUsername("admin")).thenReturn(user);

        String token = AuthService.login("admin", "1234");

        assertNotNull(token, "Token should not be null for valid login");
        assertTrue(token.contains("admin"), "Token should contain username");
    }

    @Test
    void loginShouldReturnNullWhenUserNotFound() {
        when(userDao.findByUsername("ghost")).thenReturn(null);

        String token = AuthService.login("ghost", "1234");

        assertNull(token, "Token should be null for invalid user");
    }

    @Test
    void loginShouldReturnNullWhenPasswordInvalid() {
        User user = new User();
        user.setUsername("admin");
        user.setRole("ADMIN");
        user.setPinHash(HashUtil.hashPassword("1234"));

        when(userDao.findByUsername("admin")).thenReturn(user);

        String token = AuthService.login("admin", "wrongpin");

        assertNull(token, "Token should be null for invalid password");
    }

    @Test
    void getRoleShouldReturnUserRole() {
        when(userDao.getRole("admin")).thenReturn("ADMIN");

        String role = authService.getRole("admin");

        assertEquals("ADMIN", role);
    }

    @Test
    void getEmailShouldReturnUserEmail() {
        when(userDao.getEmail("admin")).thenReturn("admin@example.com");

        String email = authService.getEmail("admin");

        assertEquals("admin@example.com", email);
    }
}
