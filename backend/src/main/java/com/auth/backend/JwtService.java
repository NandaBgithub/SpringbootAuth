package com.auth.backend;

import java.util.Base64;
import java.util.Date;
import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

    @Value("${JWTAUTHSECRET}")
    private String jwtAuthSecret;

    @Value("${JWTREFRESHSECRET}")
    private String jwtRefreshSecret;

    // Token expiration times 15mins and 7days respectively
    private final long authTokenExpirationMs = 15 * 60 * 1000;
    private final long refreshTokenExpirationMs = 7 * 24 * 60 * 60 * 1000; 

    private Key getSigningKey(String base64Secret) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Parse the token
    public Claims parseClaims(String token, String secret) {
        Key key = getSigningKey(secret);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Uses secret key to parse token and extract the username/token subject
    public String getUsernameFromToken(String token, String secret) {
        Claims claims = parseClaims(token, secret);
        return claims.getSubject();
    }

    // Validate refresh token: true if valid and not expired, false otherwise
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token, jwtRefreshSecret);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Generate new authentication (access) token from username
    public String generateAuthenticationTokenFromUsername(String username) {
        Key key = getSigningKey(jwtAuthSecret);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + authTokenExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate refresh token from username
    public String generateRefreshToken(String username) {
        Key key = getSigningKey(jwtRefreshSecret);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getJwtAuthSecret() {
        return this.jwtAuthSecret;
    }

    public String getJwtRefreshSecret() {
        return this.jwtRefreshSecret;
    }
}