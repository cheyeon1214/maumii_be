package com.project.maumii_be.jwt;

import com.project.maumii_be.domain.User;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/*
 JWT 정보 검증및 생성
 */
@Component
@Slf4j
public class JWTUtil {

    private SecretKey secretKey;//Decode한 secret key를 담는 객체
    
    //application.properties에 있는 미리 Base64로 Encode된 Secret key를 가져온다
    public JWTUtil(@Value("${spring.jwt.secret}")String secret) {
   
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }
    
    //검증 Username
    public String getUName(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().get("uName", String.class);
    }
    //검증 Id
    public String getUId(String token) {
        String uid = Jwts.parser().verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        log.debug("getUId => {}", uid);
        return uid;
    }

    //검증 Role
    public String getRole(String token) {
        String role = Jwts.parser().verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
        log.debug("getRole => {}", role);
        return role;
    }
    
    //검증 Expired
    public boolean isExpired(String token) {
        boolean expired = Jwts.parser().verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
        log.debug("isExpired => {}", expired);
        return expired;
    }
    //Bearer : JWT 혹은 Oauth에 대한 토큰을 사용
    //public String createJwt(String username, String role, Long expiredMs) {
    //claim은 payload에 해당하는 정보
    public String createJwt(User member, String role, long expiredMs) {
        Date now = new Date();
        String jwt = Jwts.builder()
                .subject(member.getUId())            // ★ uId → subject
                .claim("role", role)                 // 권한만 claim으로
                .claim("uName", member.getUName())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiredMs))
                .signWith(secretKey)
                .compact();
        log.debug("createJwt OK for uId={}", member.getUId());
        return jwt;
    }
}



