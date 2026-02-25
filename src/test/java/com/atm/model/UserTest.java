package com.atm.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testSetValidPhoneNumber() {
        User user = new User();
        user.setPhoneNumber("9876543210");
        assertEquals("9876543210", user.getPhoneNumber());
    }

    @Test
    void testSetInvalidPhoneNumberThrowsException() {
        User user = new User();
        assertThrows(IllegalArgumentException.class, () -> user.setPhoneNumber("123-abc"));
    }

    @Test
    void testSetValidEmail() {
        User user = new User();
        user.setEmail("user@example.com");
        assertEquals("user@example.com", user.getEmail());
    }

    @Test
    void testSetInvalidEmailThrowsException() {
        User user = new User();
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("invalid-email"));
    }
}
