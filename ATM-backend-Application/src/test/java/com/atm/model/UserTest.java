package com.atm.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class UserTest {
    @Test
    void testSetValidPhoneNumber() {
    	User user = new User(1, "alice", "hashedPin", "USER", 100.0, null, null);
        user.setPhoneNumber("9876543210");
        assertEquals("9876543210", user.getPhoneNumber());
    }
    @Test
    void testSetInvalidPhoneNumberThrowsException() {
    	User user = new User(1, "alice", "hashedPin", "USER", 100.0, null, null);
        assertThrows(IllegalArgumentException.class, () -> user.setPhoneNumber("123-abc"));
    }
    @Test
    void testSetValidEmail() {
        User user = new User(1, "alice", "hashedPin", "USER", 100.0, null, null);
        user.setEmail("valid@example.com");
        assertEquals("valid@example.com", user.getEmail());
    }
    @Test
    void testSetInvalidEmailThrowsException() {
        User user = new User(1, "alice", "hashedPin", "USER", 100.0, null, null);
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("v@.com"));
    }
}