package com.atm.dao;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.atm.model.Transaction;
import com.atm.util.DBUtil;

public class TransactionDao {
    private JdbcTemplate jdbcTemplate = DBUtil.getJdbcTemplate();

    public void save(Transaction tx) {
        String sql = "INSERT INTO transactions(user_id,type,amount,timestamp) VALUES(?,?,?,?)";
        jdbcTemplate.update(sql, tx.getId(), tx.getType(), tx.getAmount(), tx.getTimestamp());
    }

    public List<Transaction> findByUser(int userId) {
        String sql = "SELECT * FROM transactions WHERE user_id=?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Transaction.class), userId);
    }
}
