package dao;

import com.atm.dao.TransactionDao;
import com.atm.model.Transaction;
import com.atm.util.DBUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
        tx.setUsername("bbb");
        tx.setType("DEPOSIT");
        tx.setAmount(1000.0);
        tx.setTimestamp(new Timestamp(System.currentTimeMillis()));

        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);

            transactionDao.save(tx);

            verify(ps).setString(1, "bbb");
            verify(ps).setString(2, "DEPOSIT");
            verify(ps).setDouble(3, 1000.0);
            verify(ps).setTimestamp(eq(4), any(Timestamp.class));
            verify(ps).executeUpdate();
        }
    }

    @Test
    void testFindByUsernameReturnsTransactions() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("id")).thenReturn(1, 2);
        when(rs.getString("username")).thenReturn("bbb", "bbb");
        when(rs.getString("type")).thenReturn("DEPOSIT", "WITHDRAW");
        when(rs.getDouble("amount")).thenReturn(500.0, 200.0);
        when(rs.getTimestamp("timestamp")).thenReturn(new Timestamp(System.currentTimeMillis()));

        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);

            List<Transaction> result = transactionDao.findByUsername("bbb");

            assertEquals(2, result.size());
            assertEquals("DEPOSIT", result.get(0).getType());
            assertEquals("WITHDRAW", result.get(1).getType());
        }
    }

    @Test
    void testFindAllReturnsEmptyListWhenNoRows() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBUtil> dbMock = mockStatic(DBUtil.class)) {
            dbMock.when(DBUtil::getConnection).thenReturn(conn);

            List<Transaction> result = transactionDao.findAll();

            assertTrue(result.isEmpty());
        }
    }
}
