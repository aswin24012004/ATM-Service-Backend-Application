package com.atm.dao;

import com.atm.model.User;
import com.atm.util.DBUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserDaoTest {

    private UserDao userDao;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        userDao = new UserDao();
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
    }

    @Test
    void testFindByUsernameReturnsUser() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("username")).thenReturn("alice");
        when(rs.getString("pin_hash")).thenReturn("hash");
        when(rs.getString("role")).thenReturn("CUSTOMER");
        when(rs.getDouble("balance")).thenReturn(1000.0);
        when(rs.getString("phone_number")).thenReturn("9123456789");
        when(rs.getString("email")).thenReturn("a@b.com");

        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);

            User result = userDao.findByUsername("alice");

            assertNotNull(result);
            assertEquals("alice", result.getUsername());
            assertEquals("CUSTOMER", result.getRole());
            assertEquals(1000.0, result.getBalance());
        }
    }

    @Test
    void testGetRole() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("role")).thenReturn("ADMIN");

        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            String role = userDao.getRole("bob");
            assertEquals("ADMIN", role);
        }
    }

    @Test
    void testGetEmail() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("email")).thenReturn("bob@example.com");

        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            String email = userDao.getEmail("bob");
            assertEquals("bob@example.com", email);
        }
    }

    @Test
    void testUpdateBalance() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            userDao.updateBalance("alice", 2000.0);
            verify(ps, times(1)).setDouble(1, 2000.0);
            verify(ps, times(1)).setString(2, "alice");
            verify(ps, times(1)).executeUpdate();
        }
    }

    @Test
    void testLogTransaction() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            userDao.logTransaction("alice", "DEPOSIT", 500.0);
            verify(ps, times(1)).setString(1, "alice");
            verify(ps, times(1)).setString(2, "DEPOSIT");
            verify(ps, times(1)).setDouble(3, 500.0);
            verify(ps, times(1)).executeUpdate();
        }
    }
}
