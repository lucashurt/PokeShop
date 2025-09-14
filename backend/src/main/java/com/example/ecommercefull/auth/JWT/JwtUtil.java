package com.example.ecommercefull.auth.JWT;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${JWT_SECRET}")
    private String jwtSecret;
    @Value("${JWT_EXPIRATION}")
    private long jwtExpiration;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username, String role,Long userId){
        return Jwts.builder()
                .subject(username)
                .claim("role",role)
                .claim("userId",userId)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(jwtExpiration,ChronoUnit.SECONDS)))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims getClaims(String token){
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token){
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        }
        catch (Exception e){
            return false;
        }
    }

    public String getUsernameFromToken(String token){
        return getClaims(token).getSubject();
    }

    public String getRole(String token){
        return getClaims(token).get("role",String.class);
    }

    public Long getUserIdFromToken(String token){
        return getClaims(token).get("userId",Long.class);
    }
}
