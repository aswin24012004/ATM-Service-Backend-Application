package com.atm.dao;

import com.atm.model.Transaction;
import com.atm.util.DBUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionDaoTest {

    private TransactionDao transactionDao;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        transactionDao = new TransactionDao();
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
    }

    @Test
    void testSaveTransaction() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        Transaction tx = new Transaction();
        tx.setUsername("alice");
        tx.setType("DEPOSIT");
        tx.setAmount(1000.0);
        tx.setTimestamp(new Timestamp(System.currentTimeMillis()));

        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            transactionDao.save(tx);
            verify(ps, times(1)).setString(1, "alice");
            verify(ps, times(1)).setString(2, "DEPOSIT");
            verify(ps, times(1)).setDouble(3, 1000.0);
            verify(ps, times(1)).setTimestamp(eq(4), any(Timestamp.class));
            verify(ps, times(1)).executeUpdate();
        }
    }

    @Test
    void testFindByUsername() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("id")).thenReturn(1, 2);
        when(rs.getString("username")).thenReturn("alice", "alice");
        when(rs.getString("type")).thenReturn("DEPOSIT", "WITHDRAW");
        when(rs.getDouble("amount")).thenReturn(500.0, 200.0);
        when(rs.getTimestamp("timestamp")).thenReturn(new Timestamp(System.currentTimeMillis()));

        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            List<Transaction> result = transactionDao.findByUsername("alice");
            assertEquals(2, result.size());
            assertEquals("DEPOSIT", result.get(0).getType());
            assertEquals("WITHDRAW", result.get(1).getType());
        }
    }

    @Test
    void testFindAll() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);
            List<Transaction> result = transactionDao.findAll();
            assertEquals(0, result.size());
        }
    }
}
