package com.atm.dao;

import com.atm.model.ATM;
import com.atm.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ATMDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(ATMDao.class);

    public ATM getATMById(int id) {
        String sql = "SELECT * FROM atm WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ATM(
                        rs.getInt("id"),
                        rs.getDouble("total_balance")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error fetching ATM with id {}: {}", id, e.getMessage(), e);
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
            LOGGER.info("Updated balance for ATM id {} to {}", id, newBalance);
        } catch (SQLException e) {
            LOGGER.error("Error updating balance for ATM id {}: {}", id, e.getMessage(), e);
        }
    }

    public void addAmount(int id, double amount) {
        String sql = "UPDATE atm SET total_balance = total_balance + ? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, id);
            ps.executeUpdate();
            LOGGER.info("Added {} to ATM id {}", amount, id);
        } catch (SQLException e) {
            LOGGER.error("Error adding amount to ATM id {}: {}", id, e.getMessage(), e);
        }
    }

    public void checkBalance(int id, double amount) {
        String sql = "UPDATE atm SET total_balance = total_balance - ? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, id);
            ps.executeUpdate();
            LOGGER.info("Deducted {} from ATM id {}", amount, id);
        } catch (SQLException e) {
            LOGGER.error("Error deducting amount from ATM id {}: {}", id, e.getMessage(), e);
        }
    }
}
