package dao;


import java.sql.*;

import db.DBConnection;
import model.User;

public class UserDAO {

	public static void save(User u) throws SQLException, Exception {
	    String sql = "INSERT INTO user(name, email, password) VALUES (?, ?, ?)";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setString(1, u.getName());
	        ps.setString(2, u.getEmail());
	        ps.setString(3, u.getPassword()); 
	        ps.executeUpdate();
	    }
	}
	
	
	
    public static User findByName(String name) {
        String sql = "SELECT * FROM user WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}