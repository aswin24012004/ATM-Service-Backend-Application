package com.atm.service;


import org.springframework.jdbc.core.JdbcTemplate;

import com.atm.dao.UserDao;
import com.atm.model.User;
import com.atm.util.DBUtil;
import com.atm.util.HashUtil;
import com.atm.util.TokenUtil;

public class AuthService {
	
	private static UserDao userDao = new UserDao();
    public static String login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user != null && HashUtil.checkPassword(password, user.getPinHash())) {
            return TokenUtil.generateToken(user.getUsername(), user.getRole());
        }
        return null;
    }
    private JdbcTemplate jdbc = DBUtil.getJdbcTemplate();
    public String getRole(String username) { 
    	return userDao.getRole(username);
    }
    public String getEmail(String username) { 
    	return userDao.getEmail(username);
    }
    
	/*
	 * public static void main(String[] args) { String name = "admin"; String pin =
	 * "1234";
	 * 
	 * System.out.println(HashUtil.hashPassword(pin));
	 * 
	 * try { String token = AuthService.login(name, pin); if (token != null) {
	 * System.out.println("Token: " + token); System.out.println("Login success"); }
	 * else { System.out.println("Login failed"); } } catch (Exception e) {
	 * System.out.println("Error: " + e.getMessage()); } }
	 */

}
