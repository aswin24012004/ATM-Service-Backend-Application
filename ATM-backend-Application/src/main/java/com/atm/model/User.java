package com.atm.model;

import com.atm.service.RegexService;

public class User {
    private int id;
    private String username;
    private String pinHash;
    private String role;
    private double balance;
    private String phoneNumber;
    private String email;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
        if (RegexService.isValidPhone(phoneNumber)) {
            this.phoneNumber = RegexService.normalizePhone(phoneNumber);
        } else {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (RegexService.isValidEmail(email)) {
            this.email = email;
        } else {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }
}
