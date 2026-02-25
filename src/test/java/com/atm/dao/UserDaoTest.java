package com.atm.dao;

import com.atm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        // Override the jdbc inside UserDao with our mock
        userDao = new UserDao() {
            { this.jdbc = jdbcTemplate; }
        };
    }

    @Test
    void testFindByUsernameReturnsUser() {
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("alice");
        mockUser.setRole("CUSTOMER");
        mockUser.setBalance(1000.0);

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDao.UserRowMapper.class)))
                .thenReturn(mockUser);

        User result = userDao.findByUsername("alice");

        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        assertEquals("CUSTOMER", result.getRole());
        assertEquals(1000.0, result.getBalance());

        verify(jdbcTemplate, times(1))
                .queryForObject(eq("SELECT * FROM users WHERE username=?"), any(Object[].class), any(UserDao.UserRowMapper.class));
    }

    @Test
    void testGetRole() {
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(String.class)))
                .thenReturn("ADMIN");

        String role = userDao.getRole("bob");

        assertEquals("ADMIN", role);
        verify(jdbcTemplate, times(1))
                .queryForObject(eq("SELECT role FROM users WHERE username=?"), any(Object[].class), eq(String.class));
    }

    @Test
    void testGetEmail() {
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(String.class)))
                .thenReturn("bob@example.com");

        String email = userDao.getEmail("bob");

        assertEquals("bob@example.com", email);
        verify(jdbcTemplate, times(1))
                .queryForObject(eq("SELECT email FROM users WHERE username=?"), any(Object[].class), eq(String.class));
    }

    @Test
    void testUpdateBalance() {
        userDao.updateBalance("alice", 2000.0);

        verify(jdbcTemplate, times(1))
                .update(eq("UPDATE users SET balance=? WHERE username=?"), eq(2000.0), eq("alice"));
    }

    @Test
    void testLogTransaction() {
        userDao.logTransaction("alice", "DEPOSIT", 500.0);

        verify(jdbcTemplate, times(1))
                .update(eq("INSERT INTO transactions(username, type, amount) VALUES(?, ?, ?)"),
                        eq("alice"), eq("DEPOSIT"), eq(500.0));
    }
}
