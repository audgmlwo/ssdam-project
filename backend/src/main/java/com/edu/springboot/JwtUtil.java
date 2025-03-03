package com.edu.springboot;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey SECRET_KEY;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            this.SECRET_KEY = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT Secret Key가 올바른 Base64 인코딩 형식이 아닙니다. `jwt.secret` 값을 확인하세요.", e);
        }
    }

    // JwtDecoder에서 사용하기 위한 getSecretKey() 메서드
    public SecretKey getSecretKey() {
        return SECRET_KEY;
    }

    // HTTP 요청에서 JWT 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // JWT 토큰 생성
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24시간 후 만료
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT에서 이메일(사용자명) 추출
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // JWT 유효성 검사
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
          
            if (claims.getExpiration().before(new Date())) {              
                return false;
            }

            // 서명 검증을 위해 디코딩 후 서명 확인
            Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) 
                .build()
                .parseClaimsJws(token);
            
            System.out.println("✅ JWT 서명 검증 성공!");
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT 만료됨: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("JWT 서명 불일치: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("JWT 형식 오류: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("JWT 검증 실패 (기타 오류): " + e.getMessage());
        }
        return false;
    }

    // JWT에서 Claims(페이로드 정보) 추출
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
