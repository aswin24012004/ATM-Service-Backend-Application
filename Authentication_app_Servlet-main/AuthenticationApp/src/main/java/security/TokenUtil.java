package security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.*;


public class TokenUtil {
	private static final String SECRET = "your_super_secret_key_32_characters_long";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Function 1: Create the token (During Login)
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) 
//                .signWith( SignatureAlgorithm.HS256,KEY) 
                .signWith(KEY)
                .compact();
    }

    public static String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }


}
