package com.atm.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenUtilTest {

    @Test
    void testGenerateAndValidateToken() {
        String username = "alice";
        String role = "CUSTOMER";

        String token = TokenUtil.generateToken(username, role);
        assertNotNull(token);

        boolean isValid = TokenUtil.validateToken(token);
        assertTrue(isValid);

        Claims claims = TokenUtil.getClaims(token);
        assertEquals(username, claims.getSubject());
        assertEquals(role, claims.get("role"));
    }

    @Test
    void testInvalidTokenFailsValidation() {
        String invalidToken = "this.is.not.a.valid.token";
        boolean isValid = TokenUtil.validateToken(invalidToken);
        assertFalse(isValid);
    }

    @Test
    void testExpiredTokenFailsValidation() throws InterruptedException {
        String token = TokenUtil.generateToken("admin", "ADMIN");

        Thread.sleep(2000);

        boolean isValid = TokenUtil.validateToken(token);
        assertFalse(isValid);
    }
}
