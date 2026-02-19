package com.atm.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class TokenUtil {

    private static final String SECRET = ConfigUtil.get("SECRET");   // 32 chars
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_MS = Long.parseLong(ConfigUtil.get("EXPIRATION_MS")); //  3600000 (1 hour)

    public static String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(KEY)
                .compact();
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.err.println("Validation Error: " + e.getMessage());
            return false;
        }
    }

    public static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static void main(String[] args) {
        String user = "admin";
        String role = "ADMIN";

        String token = generateToken(user, role);
        System.out.println("Generated JWT: " + token);

        boolean isValid = validateToken(token);
        System.out.println("Is token valid? " + isValid);

        Claims claims = getClaims(token);
        System.out.println("Username from token: " + claims.getSubject());
        System.out.println("Role from token: " + claims.get("role"));
    }
}
