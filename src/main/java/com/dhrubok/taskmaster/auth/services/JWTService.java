package com.dhrubok.taskmaster.auth.services;

import com.dhrubok.taskmaster.auth.constants.SecurityConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    private final SecretKey secretKey = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(SecurityConstant.JWT_SECRET)
    );

    // 🔹 1. Generate Access Token (Short Lived)
    public String generateAccessToken(String email) {
        return buildToken(new HashMap<>(), email, SecurityConstant.JWT_EXPIRATION_MILLIS);
    }

    // 🔹 2. Generate Refresh Token (Long Lived)
    public String generateRefreshToken(String email) {
        return buildToken(new HashMap<>(), email, SecurityConstant.REFRESH_EXPIRATION_MILLIS);
    }

    // 🔹 Helper Method to Build Token (Prevents Code Duplication)
    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject.toLowerCase())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    // 🔹 Validate token (Checks username and expiry)
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUserName(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // Overload for UserDetails (used in JwtFilter)
    public boolean validateToken(String token, UserDetails userDetails) {
        return validateToken(token, userDetails.getUsername());
    }

    // 🔹 Extract username from JWT
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 🔹 Check if token expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 🔹 Extract specific claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 🔹 Get all claims from JWT
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}