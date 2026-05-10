package com.japanesenote.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // secret key — in production store this in application.properties
    private static final String SECRET = "your-very-long-secret-key-at-least-32-chars!!";
    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 24; // 24 hours
                                                                      
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // generate a token for a given username
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getKey())
                .compact();
    }

    // extract username from token
    public String extractUsername(String token) {
        // token contains header, payload, signature
        // parseSignedClaims will use secret key to recompute header & payload to check
        // the output signature is the same as input signature
        // and also check the expiration

        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()                                                                      
                .getSubject();
    }                                                                                              
                
    // validate token — returns true if valid and not expired
    public boolean validateToken(String token) {
        // TODO: try calling extractUsername(token)
        //       return true if no exception is thrown
        //       return false if an exception is thrown (expired, tampered, etc.)

        String extracted;
        try {
            extracted = extractUsername(token);
            if (extracted != null) return true;

        } catch (Exception e) {
            return false;
        }
        return false;
    }
}