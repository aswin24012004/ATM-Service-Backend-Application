package service;

import org.junit.jupiter.api.Test;

import com.atm.service.RegexService;

import static org.junit.jupiter.api.Assertions.*;

class RegexServiceTest {

    @Test
    void testNormalizePhonePadsShortNumber() {
        String input = "98765"; 
        String normalized = RegexService.normalizePhone(input);
        assertEquals("9876500000", normalized); 
    }

    @Test
    void testNormalizePhoneTrimsLongNumber() {
        String input = "9876543210123"; 
        String normalized = RegexService.normalizePhone(input);
        assertEquals("9876543210", normalized); 
    }

    @Test
    void testNormalizePhoneExactTenDigits() {
        String input = "9876543210"; 
        String normalized = RegexService.normalizePhone(input);
        assertEquals("9876543210", normalized); 
    }

    @Test
    void testNormalizePhoneWithNonDigits() {
        String input = "(987) 654-3210"; 
        String normalized = RegexService.normalizePhone(input);
        assertEquals("9876543210", normalized); 
    }

    @Test
    void testNormalizePhoneNullInput() {
        assertNull(RegexService.normalizePhone(null));
    }
}
