package com.atm.service;

import com.atm.dao.UserDao;
import com.atm.model.User;
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
    // Get role from DAO
    public String getRole(String username) {
        return userDao.getRole(username);
    }
    // Get email from DAO
    public String getEmail(String username) {
        return userDao.getEmail(username);
    }
 
}
