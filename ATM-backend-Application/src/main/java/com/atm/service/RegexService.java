package com.atm.service;

import java.util.regex.Pattern;

public class RegexService {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    // Validate the phone number
    public static boolean isValidPhone(String phoneNumber) {
        if (phoneNumber == null) return false;
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        return PHONE_PATTERN.matcher(digitsOnly).matches();
    }

    public static String normalizePhone(String phoneNumber) {
        if (phoneNumber == null) return null;
        String digitsOnly = phoneNumber.replaceAll("\\D", "");

        if (digitsOnly.length() < 10) {
            digitsOnly = String.format("%-10s", digitsOnly).replace(' ', '0');
        }
        if (digitsOnly.length() > 10) {
            digitsOnly = digitsOnly.substring(0, 10);
        }
        return digitsOnly;
    }

    // Validate the email
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
}

