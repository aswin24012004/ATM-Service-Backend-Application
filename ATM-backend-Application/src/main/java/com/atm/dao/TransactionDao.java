package com.atm.dao;

import com.atm.model.Transaction;
import com.atm.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionDao {
	private static final Logger logger = LoggerFactory.getLogger(TransactionDao.class);
    public void save(Transaction tx) {
        String sql = "INSERT INTO transactions(username,type,amount,timestamp) VALUES(?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tx.getUsername());
            ps.setString(2, tx.getType());
            ps.setDouble(3, tx.getAmount());
            ps.setTimestamp(4, new java.sql.Timestamp(tx.getTimestamp().getTime()));
            ps.executeUpdate();
        } catch (SQLException e) {
        	logger.info(e.getMessage());
        }
    }

    public List<Transaction> findByUsername(String username) {
        String sql = "SELECT * FROM transactions WHERE username=? ORDER BY timestamp DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction();
                    tx.setId(rs.getInt("id"));
                    tx.setUsername(rs.getString("username"));
                    tx.setType(rs.getString("type"));
                    tx.setAmount(rs.getDouble("amount"));
                    tx.setTimestamp(rs.getTimestamp("timestamp"));
                    list.add(tx);
                }
            }
        } catch (SQLException e) {
        	logger.info(e.getMessage());
        }
        return list;
    }

    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Transaction tx = new Transaction();
                tx.setId(rs.getInt("id"));
                tx.setUsername(rs.getString("username"));
                tx.setType(rs.getString("type"));
                tx.setAmount(rs.getDouble("amount"));
                tx.setTimestamp(rs.getTimestamp("timestamp"));
                list.add(tx);
            }
        } catch (SQLException e) {
        	logger.info(e.getMessage());
        }
        return list;
    }
}
