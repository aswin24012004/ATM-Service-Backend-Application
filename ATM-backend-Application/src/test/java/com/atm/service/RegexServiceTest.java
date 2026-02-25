package com.atm.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegexServiceTest {

    @Test
    void testValidPhoneNumber() {
        assertTrue(RegexService.isValidPhone("9876543210"));
    }

    @Test
    void testInvalidPhoneNumber() {
        assertFalse(RegexService.isValidPhone("123-abc"));
    }

    @Test
    void testNormalizePhoneNumber() {
        assertEquals("9876543210", RegexService.normalizePhone("987-654-3210"));
    }

    @Test
    void testValidEmail() {
        assertTrue(RegexService.isValidEmail("user@example.com"));
    }

    @Test
    void testInvalidEmail() {
        assertFalse(RegexService.isValidEmail("invalid-email"));
    }
}
