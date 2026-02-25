package com.atm.dao;

import com.atm.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionDaoTest {

    private JdbcTemplate jdbcTemplate;
    private TransactionDao transactionDao;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        // Override the jdbcTemplate inside TransactionDao with our mock
        transactionDao = new TransactionDao() {
            { this.jdbcTemplate = jdbcTemplate; }
        };
    }

    @Test
    void testSaveTransaction() {
        Transaction tx = new Transaction();
        tx.setId(1);
        tx.setType("DEPOSIT");
        tx.setAmount(1000.0);
        tx.setTimestamp(new Timestamp(System.currentTimeMillis()));

        transactionDao.save(tx);

        verify(jdbcTemplate, times(1)).update(
                eq("INSERT INTO transactions(user_id,type,amount,timestamp) VALUES(?,?,?,?)"),
                eq(tx.getId()), eq(tx.getType()), eq(tx.getAmount()), eq(tx.getTimestamp())
        );
    }

    @SuppressWarnings("unchecked")
	@Test
    void testFindByUser() {
        Transaction tx1 = new Transaction();
        tx1.setId(1);
        tx1.setType("DEPOSIT");
        tx1.setAmount(500.0);
        tx1.setTimestamp(new Timestamp(System.currentTimeMillis()));

        Transaction tx2 = new Transaction();
        tx2.setId(1);
        tx2.setType("WITHDRAW");
        tx2.setAmount(200.0);
        tx2.setTimestamp(new Timestamp(System.currentTimeMillis()));

        List<Transaction> mockResult = Arrays.asList(tx1, tx2);

        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), eq(1)))
                .thenReturn(mockResult);

        List<Transaction> result = transactionDao.findByUser(1);

        assertEquals(2, result.size());
        assertEquals("DEPOSIT", result.get(0).getType());
        assertEquals("WITHDRAW", result.get(1).getType());

        verify(jdbcTemplate, times(1))
                .query(eq("SELECT * FROM transactions WHERE user_id=?"),
                       any(BeanPropertyRowMapper.class), eq(1));
    }
}
