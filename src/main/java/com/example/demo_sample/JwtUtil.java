package com.example.demo_sample;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "MySuperSecretKeyForJWTGeneration12345"; // >32 chars

    // Thời hạn riêng cho Access Token và Refresh Token
    private final long ACCESS_TOKEN_EXP_MS = 1000 * 60 * 15; // 15 phút
    private final long REFRESH_TOKEN_EXP_MS = 1000 * 60 * 60 * 24 * 7; // 7 ngày

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Sinh Access Token
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date()) //Tgian tạo token
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXP_MS)) //Tgian hết hạn
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) //gán token
                .compact();
    }

    // Sinh Refresh Token
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Lấy username từ token
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Kiểm tra token hợp lệ
    public boolean isTokenValid(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    // Kiểm tra token hết hạn
    public boolean isTokenExpired(String token) {
        return extractExpiration(token) < System.currentTimeMillis();
    }

    // Lấy thời hạn token
    private long extractExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .getTime();
    }
}