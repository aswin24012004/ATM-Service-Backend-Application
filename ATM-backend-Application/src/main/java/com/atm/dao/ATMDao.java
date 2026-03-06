package com.atm.dao;

import com.atm.model.ATM;
import com.atm.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ATMDao {
    
    public ATM getATMById(int id) {
        String sql = "SELECT * FROM atm WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ATM atm = new ATM();
                    atm.setId(rs.getInt("id"));
                    atm.setTotalBalance(rs.getDouble("total_balance"));
                    return atm;
                }
            }
        } catch (SQLException e) {
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

    public void addFunds(int id, double amount) {
        String sql = "UPDATE atm SET total_balance = total_balance + ? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
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
        }
    }
}
