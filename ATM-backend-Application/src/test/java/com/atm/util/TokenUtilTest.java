package com.atm.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenUtilTest {
    private MockedStatic<ConfigUtil> configMock;
    
    @BeforeEach
    void setUp() {
        configMock = mockStatic(ConfigUtil.class);
        configMock.when(() -> ConfigUtil.get("SECRET"))
                  .thenReturn("12345678901234567890123456789012");
        configMock.when(() -> ConfigUtil.get("EXPIRATION_MS"))
                  .thenReturn("3600000");
    }


    @AfterEach
    void tearDown() {
        configMock.close();
    }
    @Test
    void testGenerateAndValidateToken() {
        String token = TokenUtil.generateToken("aswin", "USER");
        assertTrue(TokenUtil.validateToken(token));
        Claims claims = TokenUtil.getClaims(token);
        assertEquals("aswin", claims.getSubject());
        assertEquals("USER", claims.get("role"));
    }
    @Test
    void testInvalidTokenFailsValidation() {
        assertFalse(TokenUtil.validateToken("not.a.real.token"));
    }
    @Test
    void testTokenExpiration() throws InterruptedException {
        configMock.when(() -> ConfigUtil.get("EXPIRATION_MS")).thenReturn("1"); // 1 ms
        String token = TokenUtil.generateToken("admin", "ADMIN");
        Thread.sleep(5);
        assertFalse(TokenUtil.validateToken(token));
    }
}
