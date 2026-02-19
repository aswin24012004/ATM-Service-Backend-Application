package com.atm.util;

import org.mindrot.jbcrypt.BCrypt;

public class HashUtil {
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }

    public static void main(String[] args) {

        String userPassword = "1234";
        
//         1. Hash the password 
        long startHash = System.currentTimeMillis();
        String hashedPassword = hashPassword(userPassword);
        long endHash = System.currentTimeMillis();

        System.out.println("Plain Password: " + userPassword);
        System.out.println("Hashed Password (Store this in DB): " + hashedPassword);
        System.out.println("Time taken to hash: " + (endHash - startHash) + "ms");


        String plain = "admin123"; 
//        String hashed = "$2a$10$f2dHxBsq/mQRVdN76npytenNOYvfLGEwBAUPujyXWXGXkb1BNOjSS";
        String hashed = hashPassword(plain);
        System.out.println("Hashed-> "+hashed);

        boolean match = BCrypt.checkpw(plain, hashed);

        if (match) {
            System.out.println("Password is correct!");
        } else {
            System.out.println("Password is invalid.");
        }

//         Correct 
        boolean isMatch = checkPassword(userPassword, hashedPassword);
        System.out.println("Login with correct password: " + (isMatch ? "SUCCESS" : "FAILED"));

    }
}
