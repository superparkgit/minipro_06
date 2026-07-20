package com.mycom.myapp.domain.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

/**
 * JWT Access Token과 Refresh Token을 생성하고 검증.
 * HTTP 헤더에서 토큰을 꺼내거나 인증객체를 만드는 작업은
 * JwtAuthenticationFilter에서 처리.
 */
@Component
public class JwtUtil {
	private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";
    
    /**
     * application.properties 에서 읽어오는 JWT 비밀키 문자열.
     * 실제 운영 시 환경변수 JWT_SECRET 값으로 교체.
     */
    @Value("${jwt.secret}")
    private String secretKeyStr;
    
    /**
     * JWT 서명 생성과 검증에 사용하는 키.
     */
    private SecretKey secretKey;
    
    /**
     * Access Token 유효시간.
     */
    @Value("${jwt.access-token-expiration}")
    private Duration accessTokenExpiration;
    
    /**
     * Refresh Token 유효시간.
     */
    @Value("${jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;
    
    /**
     * JwtUtil 생성 직후 호출.
     */
    @PostConstruct
    protected void init() {
    	secretKey = Keys.hmacShaKeyFor(
    				secretKeyStr.getBytes(StandardCharsets.UTF_8)
    			);
    }
    
    
    /**
     * 기본 인증에 사용할 AccessToken 생성.
     * @param username 로그인 식별자인 이메일
     * @return 생성된 Access Token
     */
    public String createAccessToken(String username) {
    	return createToken(username, ACCESS_TOKEN_TYPE, accessTokenExpiration);
    }
    
    /**
     * Access Token 재발급에 사용할
     * Refresh Token을 생성.
     * @param username 로그인 식별자인 이메일
     * @return 생성된 Refresh Token
     */
    public String createRefreshToken(String username) {
    	return createToken(username, REFRESH_TOKEN_TYPE, refreshTokenExpiration);
    }
    
    /**
     * 토큰 소유자, 토큰 종류, 유효시간을 전달받아 JWT 생성
     */
    private String createToken(String username, String tokenType, Duration tokenValidDuration) {
    	Instant now = Instant.now();
    	Instant expiration = now.plus(tokenValidDuration);
    	
    	return Jwts.builder()
    			.subject(username) // payload : 사용자 식별자 이메일 (sub)
    			.claim(TOKEN_TYPE_CLAIM, tokenType) // Access Token과 Refresh Token 구분
    			.issuedAt(Date.from(now)) // payload : 발급 시각 (iat)
    			.expiration(Date.from(expiration)) // payload : 만료 시각 (exp)
    			.signWith(secretKey, Jwts.SIG.HS256) // HS256 방식으로 서명
    			.compact(); // JWT 문자열 생성 
    	
    }
    
    /**
     * Access Token의 서명, 만료시간, 토큰 종류를 검증.
     * 
     */
    public Claims validateAccessToken(String token) {
    	return validateToken(token, ACCESS_TOKEN_TYPE);
    }
    
    /**
     * Refresh Token의 서명, 만료시간, 토큰 종류를 검증.
     */
    public Claims validateRefreshToken(String token) {
    	return validateToken(token, REFRESH_TOKEN_TYPE);
    }
    
    /**
     * JWT의 서명과 만료시간을 검증하고 토큰 종류 확인.
     */
    private Claims validateToken(String token, String expectedTokenType) {
    	try {
    		Claims claims = parseClaims(token);
    		
    		String tokenType = claims.get(TOKEN_TYPE_CLAIM,String.class);
    		
    		// Access Token과 Refresh token을 서로 대신 사용할 수 없게.
    		if(!expectedTokenType.equals(tokenType)) {
    			return null;
    		}
    		return claims; // 유효
    	} catch (JwtException | IllegalArgumentException e){
    		/*
    		 * 만료, 잘못된 서명, 잘못된 JWT 형식 등
    		 */
    		return null;
    	}
    }
    

    /**
     * JWT를 파싱 payload의 Claims를 반환.
     */
    private Claims parseClaims(String token) {
    	return Jwts.parser()
    			.verifyWith(secretKey)
    			.build()
    			.parseSignedClaims(token)
    			.getPayload();
    }
    
    
    
    
}
