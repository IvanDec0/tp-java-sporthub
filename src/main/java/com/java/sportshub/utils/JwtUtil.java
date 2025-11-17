package com.java.sportshub.utils;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

@Component
public class JwtUtil {
    private static final Key key = Jwts.SIG.HS512.key().build(); // Change from HS256 to HS512 for better security
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 1 día

    public String generateToken(String userId) {
        return Jwts.builder().subject(userId).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)).signWith(key).compact();
    }

    public String extractUserId(String token) {
        JwtParser parser = Jwts.parser().verifyWith((SecretKey) key).build();
        Jws<Claims> jws = parser.parseSignedClaims(token);
        return jws.getPayload().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            extractUserId(token); // Si esto no lanza excepción, el token es válido
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}