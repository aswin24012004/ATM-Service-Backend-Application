package com.atm.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class TokenUtil {

    private static Key getKey() {
        String secret = ConfigUtil.get("SECRET");
        if (secret == null || secret.length() != 32) {
            throw new IllegalArgumentException("SECRET must be 32 characters long");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static long getExpirationMs() {
        String exp = ConfigUtil.get("EXPIRATION_MS");
        if (exp == null) {
            throw new IllegalArgumentException("EXPIRATION_MS must not be null");
        }
        return Long.parseLong(exp);
    }

    public static String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + getExpirationMs()))
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
