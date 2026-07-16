package com.mycom.myapp.domain.security.jwt;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mycom.myapp.domain.security.CustomUserDetailsService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 요청의 Authorization 헤더에서 Access Token을 추출하고
 * 유효한 토큰이면 Spring Security 인증 정보를 생성.
 * OncePerRequestFilter : 요청 한 개 당 한 번만 수행.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	
	private static final String BEARER_PREFIX = "Bearer ";
	
	private final JwtUtil jwtUtil;
	
	private final CustomUserDetailsService customUserDetailsService;
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		/**
		 * 1. Authrization 헤더에서 Access Token 추출
		 * 헤더가 없거나 Bearer 형식이 아니면 null 반환.
		 */
		String token = resolveToken(request);
		
		/**
		 * 2. JWT 서명, 만료시간, 토큰 종류 검증
		 * 유효하지 않으면 Claims는 null
		 */
		Claims claims = (token != null) ? jwtUtil.validateAccessToken(token) : null;
		
		/**
		 * 3. 유효한 JWT라면 DB에서 회원을 다시 조회.
		 * 이미 인증 정보가 있는 경우 중복 인증 X
		 */
		if(claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				/**
				 * JWT subject에 저장한 이메일을 꺼냄.
				 */
				String email = claims.getSubject();
				
				/** 
				 * DB에서 회원을 다시 조회.
				 * 회원 탈퇴 여부와 현재 역할을 확인하고
				 * CustomUserDetails를 반환.
				 */
				UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
				
				/**
				 * Spring Security가 사용하는 인증 객체를 생성.
				 * JwtUtil 에 있던 public UsernamePasswordAuthenticationToken getAuthentication(String token)
				 */
				UsernamePasswordAuthenticationToken authenticationToken = 
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				
				/**
				 * 현재 요청의 IP, 세션 ID 등 부가 정보를 저장.
				 */
				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				
				/**
				 * 현재 요청이 처리되는 동안 사용할 Spring Security 공용 인증 저장소에 저장
				 */
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			} catch (UsernameNotFoundException e) {
				/**
				 * 토큰은 유효하지만 DB에서 회원을 찾을 수 없는 경우
				 * 인증 정보 저장 X
				 */
				SecurityContextHolder.clearContext();
			}
		}
		/**
		 * 다음 필터로 요청 전달.
		 * 인증 필요한 URL인데 인증 정보가 없다면
		 * Spring Security에서 접근 거부
		 */
		filterChain.doFilter(request, response);
	}
	
	/**
	 * Authorization: Bearer {token} 형식에서 
	 * JWT 문자열만 추출.
	 */
	/*
	 * 기존 수업
	 * JwtUtil getTokenFromHeader(...) -> X-AUTH-TOKEN: ...
	 * 현재
	 * Authorization: Bearer ...
	 */
	private String resolveToken(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if(StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
			return authorizationHeader.substring(BEARER_PREFIX.length());
		}
		return null;
	}
}
