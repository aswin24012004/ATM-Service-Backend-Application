package com.atm.dao;

import com.atm.dao.UserDao;
import com.atm.model.User;
import com.atm.util.DBUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class UserDaoTest {
    private UserDao userDao;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;
    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
    } 
    @Test
    void testFindByUsernameReturnsUser() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("username")).thenReturn("bbb");
        when(rs.getString("pin_hash")).thenReturn("hashedPin");
        when(rs.getString("role")).thenReturn("USER");
        when(rs.getDouble("balance")).thenReturn(100.0);
        when(rs.getString("phone_number")).thenReturn("6667778889");
        when(rs.getString("email")).thenReturn("a@.gmail.com");
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            User user = userDao.findByUsername("bbb");
            assertNotNull(user);
            assertEquals("bbb", user.getUsername());
            assertEquals("USER", user.getRole());
            assertEquals(100.0, user.getBalance());
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
            String role = userDao.getRole("admin");
            assertEquals("ADMIN", role);
        }
    }
    @Test
    void testGetEmail() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("email")).thenReturn("a@.gmail.com");
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            String email = userDao.getEmail("bbb");
            assertEquals("a@.gmail.com", email);
        }
    }
    @Test
    void testGetBalance() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDouble("balance")).thenReturn(250.0);
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            double balance = userDao.getBalance("aaa");
            assertEquals(250.0, balance);
        }
    }
    @Test
    void testUpdateBalance() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            userDao.updateBalance("aaa", 500.0);
            verify(ps).setDouble(1, 500.0);
            verify(ps).setString(2, "aaa");
            verify(ps).executeUpdate();
        }
    }
    @Test
    void testLogTransaction() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            userDao.logTransaction("bbb", "DEPOSIT", 100.0);
            verify(ps).setString(1, "bbb");
            verify(ps).setString(2, "DEPOSIT");
            verify(ps).setDouble(3, 100.0);
            verify(ps).executeUpdate();
        }
    }
    @Test
    void testCreateUserSuccess() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            boolean created = userDao.createUser("Jully", "hash", "USER", 0.0, "9234567890", "jully@gmail.com");
            assertTrue(created);
        }
    }
    @Test
    void testGetAllUsersReturnsList() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("username")).thenReturn("bbb");
        when(rs.getString("role")).thenReturn("USER");
        when(rs.getDouble("balance")).thenReturn(100.0);
        when(rs.getString("phone_number")).thenReturn("6667778889");
        when(rs.getString("email")).thenReturn("a@.gmail.com");
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            List<Map<String, Object>> users = userDao.getAllUsers();
            assertEquals(1, users.size());
            assertEquals("bbb", users.get(0).get("username"));
        }
    }
}
