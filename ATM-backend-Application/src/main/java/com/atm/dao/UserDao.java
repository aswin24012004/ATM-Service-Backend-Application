package com.atm.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import com.atm.model.User;
import com.atm.util.DBUtil;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
    private JdbcTemplate jdbc = DBUtil.getJdbcTemplate();

    public User findByUsername(String username) {
        try {
            return jdbc.queryForObject("SELECT * FROM users WHERE username=?", new Object[]{username}, new UserRowMapper());
        } catch (Exception e) {
            return null;
        }
    }
    public String getRole(String username) { 
    	try { 
    		return jdbc.queryForObject( "SELECT role FROM users WHERE username=?", new Object[]{username}, String.class ); 
    		} catch (Exception e) {
    			  return null;
    	}
    }
    
    public void updateBalance(String username, double balance) {
        jdbc.update("UPDATE users SET balance=? WHERE username=?", balance, username);
    }

    public void logTransaction(String username, String type, double amount) {
        jdbc.update("INSERT INTO transactions(username, type, amount) VALUES(?, ?, ?)", username, type, amount);
    }


    private static class UserRowMapper implements RowMapper<User> {
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(rs.getString("pin_hash"));
            user.setRole(rs.getString("role"));
            user.setBalance(rs.getDouble("balance"));
            return user;
        }
    }
}

