package com.atm.dao;

import com.atm.model.ATM;
import com.atm.util.DBUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ATMDao {
    private JdbcTemplate jdbc = DBUtil.getJdbcTemplate();

    
	public ATM getATMById(int id) {
        return jdbc.queryForObject("SELECT * FROM atm WHERE id=?", new Object[]{id}, new ATMRowMapper());
    }

    public void updateBalance(int id, double newBalance) {
        jdbc.update("UPDATE atm SET total_balance=? WHERE id=?", newBalance, id);
    }

    public void addFunds(int id, double amount) {
        jdbc.update("UPDATE atm SET total_balance = total_balance + ? WHERE id=?", amount, id);
    }

    public void checkBalance(int id, double amount) {
        jdbc.update("UPDATE atm SET total_balance = total_balance - ? WHERE id=?", amount, id);
    }

    private static class ATMRowMapper implements RowMapper<ATM> {
        public ATM mapRow(ResultSet rs, int rowNum) throws SQLException {
            ATM atm = new ATM();
            atm.setId(rs.getInt("id"));
            atm.setTotalBalance(rs.getDouble("total_balance"));
            return atm;
        }
    }
}
