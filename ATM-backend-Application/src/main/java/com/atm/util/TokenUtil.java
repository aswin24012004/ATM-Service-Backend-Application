package com.atm.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class TokenUtil { 

    private static final String DEFAULT_SECRET = "12345678901234567890123456789012"; // 32 chars
    private static final long DEFAULT_EXPIRATION_MS = 3600000L; // 1 hour

    private static Key getKey() {
        String secret = ConfigUtil.get("SECRET");
        if (secret == null || secret.isBlank()) {
            System.err.println("WARNING: SECRET not configured, using default key");
            secret = DEFAULT_SECRET;
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static long getExpirationMs() {
        String value = ConfigUtil.get("EXPIRATION_MS");
        if (value == null || value.isBlank()) {
            System.err.println("WARNING: EXPIRATION_MS not configured, using default 1 hour");
            return DEFAULT_EXPIRATION_MS;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            System.err.println("Invalid EXPIRATION_MS value, using default 1 hour");
            return DEFAULT_EXPIRATION_MS;
        }
    }

    public static String generateToken(String username, String role) {
        return generateToken(username, role, getExpirationMs());
    }

    public static String generateToken(String username, String role, long expirationMs) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
