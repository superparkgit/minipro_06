package com.mycom.myapp.domain.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mycom.myapp.domain.security.handler.CustomAccessDeniedHandler;
import com.mycom.myapp.domain.security.handler.CustomAuthenticationEntryPoint;
import com.mycom.myapp.domain.security.jwt.JwtAuthenticationFilter;
import com.mycom.myapp.domain.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService customerUserDetailsService;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
											CustomAuthenticationEntryPoint entryPoint,
											CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
		return http
				// JWT 에서 httpBasic, formLogin 사용 X
				.httpBasic(httpBasic -> httpBasic.disable())
				.formLogin(formLogin -> formLogin.disable())
				// JWT를 Authorization 헤더로 전달하므로 CSRF 사용 X
				.csrf(csrf -> csrf.disable())
				// 로그인 상태를 Session에 저장 X
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(request -> request
						.requestMatchers(
								"/",
								"/index.html",
								"/error",
								"/.well-known/**"
								).permitAll()
						// Auth
						.requestMatchers(HttpMethod.POST, "/auth/signup"
														, "/auth/login").permitAll() // 회원가입, 로그인
						// Program
						.requestMatchers(HttpMethod.POST, "/programs").hasRole("TRAINER") // 프로그램 등록
						.requestMatchers(HttpMethod.PUT, "/programs/*").hasRole("TRAINER") // 프로그램 수정
						.requestMatchers(HttpMethod.DELETE, "/programs/*").hasRole("TRAINER") // 프로그램 삭제
						// Reservation
						.requestMatchers(HttpMethod.POST, "/reservations").hasRole("USER") // 예약 신청
						.requestMatchers(HttpMethod.PATCH, "/reservations/*/cancel").hasRole("USER") // 예약 취소 
						.requestMatchers(HttpMethod.PATCH, "/reservations/*/approve"
														 , "/reservations/*/reject").hasRole("TRAINER") // 예약 승인, 거절
						// Board
						.requestMatchers(HttpMethod.GET, "/posts").permitAll() // 게시글 목록 
						.requestMatchers(HttpMethod.POST, "/posts").hasAnyRole("USER", "TRAINER", "ADMIN") // 게시글 작성
						// 게시글 수정, 삭제 Service에서 사용자 확인. 
						.requestMatchers(HttpMethod.PUT, "/posts/*").authenticated() // 게시글 수정
						.requestMatchers(HttpMethod.DELETE, "/posts/*").authenticated() // 게시글 삭제
						.anyRequest().authenticated() // 위에서 정의하지 않은 요청은 로그인이 필요.
						)
				// 로그인 처리 -> 
				// 로그인 된 사용자 판별 ( token 검증 ) 이 실패한 경우
				// 우리의 코드에서 이 상황을 처리하는 처리자 필요 <- CustomAuthenticationEntryPoint
						.exceptionHandling(exceptionHandling -> 
							exceptionHandling.authenticationEntryPoint(entryPoint) // 401
											.accessDeniedHandler(accessDeniedHandler) // 403
						)
			// jwt 검증 관련 코드 
			// Spring Security의 기본 인증 필터는 UsernamePasswordAuthenticationFilter ( form login 방식 ) <= jwt 기반이므로 우리의 코드는
			// UsernamepasswordAuthenticationFilter 앞에서 처리할 필요 (filter chain 단계에서)
			// Spring Security 는 JWT 인증 필터 제공 X <= 우리가 직접 Filter 생성
						.addFilterBefore(
								new JwtAuthenticationFilter(jwtUtil, customerUserDetailsService),
								UsernamePasswordAuthenticationFilter.class)
						.build();
	}
	
}
