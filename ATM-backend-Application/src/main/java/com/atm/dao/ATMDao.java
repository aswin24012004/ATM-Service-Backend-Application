package com.atm.dao;

import com.atm.model.ATM;
import com.atm.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ATMDao {
	private static final Logger logger = LoggerFactory.getLogger(ATMDao.class); 
	public ATM getATMById(int id) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM atm WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("total_balance");
                return new ATM(id, balance);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

	
	public void updateBalance(int id, double newBalance) {
        String sql = "UPDATE atm SET total_balance=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
        }
    }
    


    public void addAmount(int id, double amount) {
        String sql = "UPDATE atm SET total_balance = total_balance + ? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
        	logger.info(e.getMessage());
        }
    }

    public void checkBalance(int id, double amount) {
        String sql = "UPDATE atm SET total_balance = total_balance - ? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
        	logger.info(e.getMessage());
        }
    }
}
