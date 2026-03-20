package com.aiboot.ecommerce.security;

import com.aiboot.ecommerce.entity.BackendUser;
import com.aiboot.ecommerce.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:ai-boot-secret-key}")
    private String secret;

    @Value("${jwt.expire-seconds:86400}")
    private Long expireSeconds;

    public String createFrontendToken(User user) {
        return createToken(user.getId(), user.getUsername(), user.getNickname(), "FRONTEND", null, user.getStatus());
    }

    public String createBackendToken(BackendUser user) {
        String roleCode = user.getRoleCode() == null || user.getRoleCode().trim().isEmpty() ? "OPERATOR" : user.getRoleCode();
        return createToken(user.getId(), user.getUsername(), user.getNickname(), "BACKEND", roleCode, user.getStatus());
    }

    public String createToken(Long userId, String username, String nickname, String userType, String roleCode, Integer status) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("nickname", nickname)
                .claim("userType", userType)
                .claim("roleCode", roleCode)
                .claim("status", status)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getExpireSeconds() {
        return expireSeconds;
    }
}
