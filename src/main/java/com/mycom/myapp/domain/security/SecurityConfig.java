package com.mycom.myapp.domain.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
	private final CustomUserDetailsService customUserDetailsService;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	/**
	 * 로그인 Service에서 이메일과 비밀번호 검증에 사용한다.
	 */
	@Bean
	AuthenticationManager authenticationManager(
	        AuthenticationConfiguration authenticationConfiguration
	) throws Exception {
	    return authenticationConfiguration.getAuthenticationManager();
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
						
						// 전체 공개
						.requestMatchers(
								"/",
								"/index.html",
								"/error",
								"/.well-known/**"
								).permitAll()
						
						// Auth 전체 공개
						.requestMatchers(HttpMethod.POST, "/api/auth/signup" // 회원가입
														, "/api/auth/login" // 로그인
														, "/api/auth/refresh" // 로큰 재발급
														, "/api/auth/logout").permitAll() // 로그아웃
						
						// GET /api/posts/{postId} 보다 먼저 선언
						.requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/posts/me").authenticated()
						
						// 공개 조회
						.requestMatchers(HttpMethod.GET, 
								// Program
								"/api/programs",
								"/api/programs/*",
								
								// Post
								"/api/posts",
								"/api/posts/*",
								
								// Comment
								"/api/posts/*/comments",
								
								// Review / Rating
								"/api/programs/*/reviews",
								"/api/trainers/*/rating",
								"/api/programs/*/rating"
								).permitAll()
						
						// Program : ROLE_TRAINER
						.requestMatchers(HttpMethod.POST, "/api/programs"
														, "/api/programs/*/trainers").hasRole("TRAINER")
						
						.requestMatchers(HttpMethod.PATCH, "/api/programs/*"
														 , "/api/programs/*/close"
														 , "/api/programs/*/open"
														 , "/api/programs/*/cancel"
														 , "/api/programs/*/complete").hasRole("TRAINER")
						
						.requestMatchers(HttpMethod.DELETE, "/api/programs/*"
														  , "/api/programs/*/trainers/*").hasRole("TRAINER")
						
						// Reservation : 로그인 사용자
						.requestMatchers(HttpMethod.GET, "/api/reservations/me").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/reservations").authenticated()
						.requestMatchers(HttpMethod.PATCH, "/api/reservations/*/cancel").authenticated()

						
						// Reservation : 담당 트레이너
						.requestMatchers(HttpMethod.GET, "/api/programs/*/reservations"
													   , "/api/stats/**").hasRole("TRAINER")
						.requestMatchers(HttpMethod.PATCH, "/api/reservations/*/approve"
													     , "/api/reservations/*/reject"
													     , "/api/reservations/*/attendance").hasRole("TRAINER")
						
						// Post / Comment : 로그인 사용자
						.requestMatchers(HttpMethod.POST, "/api/posts"
														, "/api/posts/*/comments").authenticated()
						
						.requestMatchers(HttpMethod.PUT, "/api/posts/*"
													   , "/api/comments/*").authenticated()
						
						.requestMatchers(HttpMethod.DELETE, "/api/posts/*"
														  , "/api/comments/*").authenticated()
						
						// Review
							// 예약자 본인 
						.requestMatchers(HttpMethod.POST, "/api/reservations/*/reviews").authenticated()
						
							// 작성자 본인
						.requestMatchers(HttpMethod.PUT, "/api/reviews/*").authenticated()
						
							// 담당 트레이너
						.requestMatchers(HttpMethod.POST, "/api/reviews/*/replies"
														, "/api/reviews/*/reports").hasRole("TRAINER")
						
							// 담당 트레이너 (답변 수정)
						.requestMatchers(HttpMethod.PUT, "/api/reviews/*/replies").hasRole("TRAINER")

						// Admin
						.requestMatchers("/api/admin/**").hasRole("ADMIN")

//						.anyRequest().authenticated() // 위에서 정의하지 않은 요청은 로그인이 필요.
						.anyRequest().denyAll() // 등록하지 않은 API 기본 차단
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
								new JwtAuthenticationFilter(jwtUtil, customUserDetailsService),
								UsernamePasswordAuthenticationFilter.class)
						.build();
	}
	
}
