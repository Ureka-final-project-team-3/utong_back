package com.ureka.team3.utong_backend.auth.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    
    private final JwtProperties jwtProperties;
    
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }
    
    public String generateAccessToken(String accountId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("accountId", accountId);
        claims.put("email", email);
        
        return createToken(claims, accountId, jwtProperties.getAccessTokenExpiration());
    }
    
    public String generateRefreshToken(String accountId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("accountId", accountId);
        
        return createToken(claims, accountId, jwtProperties.getRefreshTokenExpiration());
    }
    
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public String extractAccountId(String token) {
        return extractClaims(token).get("accountId", String.class);
    }
    
    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }
    
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }
    
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    public Boolean validateToken(String token, String accountId) {
        try {
            final String extractedAccountId = extractAccountId(token);
            return (extractedAccountId.equals(accountId) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }
    
    public Boolean validateToken(String token) {
        try {
            extractClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}