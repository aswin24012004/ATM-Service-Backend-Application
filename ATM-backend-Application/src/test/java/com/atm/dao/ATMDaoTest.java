package com.atm.dao;

import com.atm.model.ATM;
import com.atm.util.DBUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ATMDaoTest {

    private ATMDao atmDao;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        atmDao = new ATMDao();
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
    }

    @Test
    void testGetATMById() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getDouble("total_balance")).thenReturn(5000.0);

        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            ATM result = atmDao.getATMById(1);
            assertEquals(1, result.getId());
            assertEquals(5000.0, result.getTotalBalance());
        }
    }

    @Test
    void testUpdateBalance() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            atmDao.updateBalance(1, 2000.0);
            verify(ps, times(1)).setDouble(1, 2000.0);
            verify(ps, times(1)).setInt(2, 1);
            verify(ps, times(1)).executeUpdate();
        }
    }

    @Test
    void testAddFunds() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            atmDao.addFunds(1, 500.0);
            verify(ps, times(1)).setDouble(1, 500.0);
            verify(ps, times(1)).setInt(2, 1);
            verify(ps, times(1)).executeUpdate();
        }
    }

    @Test
    void testCheckBalance() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            atmDao.checkBalance(1, 300.0);
            verify(ps, times(1)).setDouble(1, 300.0);
            verify(ps, times(1)).setInt(2, 1);
            verify(ps, times(1)).executeUpdate();
        }
    }
}
