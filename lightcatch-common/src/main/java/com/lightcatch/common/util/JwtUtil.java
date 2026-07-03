package com.lightcatch.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private static String secret;
    private static long expireSeconds;
    private static SecretKey secretKey;

    @Value("${lightcatch.jwt.secret}")
    public void setSecret(String s) {
        secret = s;
        secretKey = Keys.hmacShaKeyFor(s.getBytes(StandardCharsets.UTF_8));
    }

    @Value("${lightcatch.jwt.expire-seconds:86400}")
    public void setExpireSeconds(long s) { expireSeconds = s; }

    public static String generateToken(String userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUserId(String token) {
        return parseToken(token).getSubject();
    }

    public static String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }
}
