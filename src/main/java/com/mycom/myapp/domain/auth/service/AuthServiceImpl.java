package com.mycom.myapp.domain.auth.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.SignupRequest;
import com.mycom.myapp.domain.auth.dto.TokenResponse;
import com.mycom.myapp.domain.auth.entity.RefreshToken;
import com.mycom.myapp.domain.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.domain.security.CustomUserDetails;
import com.mycom.myapp.domain.security.jwt.JwtUtil;
import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
	
	private static final String TOKEN_TYPE = "Bearer";
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	
	/**
	 * 이메일 중복 확인
	 * 비밀번호 암호화
	 * USER 역할로 저장
	 */
	@Override
	@Transactional
	public void signup(SignupRequest request) {
		if(userRepository.existsByEmail(request.email())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일 입니다.");
		}
		
		User user = User.builder()
						.email(request.email())
						.password(passwordEncoder.encode(request.password()))
						.name(request.name())
						.role(Role.USER)
						.build();
		
		userRepository.save(user);
	}
	
	/**
	 * AuthenticationManager 이메일, 비밀번호 검증
	 * JWT 두 개 생성
	 * Refresh Token DB 저장
	 * TokenResponse 반환
	 */
	@Override
	@Transactional // refreshtoken 저장
	public TokenResponse login(LoginRequest request) {
		
		/**
		 * AuthenticationManager 
		 * -> CustomUserDetailsService.loadUserByUsername(email)
		 * -> DB에서 User 조회
		 * -> CustomUserDetails 반환
		 * -> 비밀번호 비교
		 * -> 인증 성공
		 */
        Authentication authentication = authenticationManager.authenticate(
        		new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        // 반환 타입 Object -> 실제 타입 CustomUserDetailsService로 형 변환
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal(); // principal
        
        String accessToken = jwtUtil.createAccessToken(userDetails.getUsername());
        String refreshToken = jwtUtil.createRefreshToken(userDetails.getUsername());
        Claims refreshTokenClaims = jwtUtil.validateRefreshToken(refreshToken);
        
        // JWT 내부 exp 만료 시간 java.util.Date 타입.
        // LocalDateTime 으로 변환
        LocalDateTime expiryDate = LocalDateTime.ofInstant(
        		refreshTokenClaims.getExpiration().toInstant(), 
        		ZoneId.systemDefault());
        
        User user = userRepository.getReferenceById(userDetails.getUserId());
        
        RefreshToken savedRefreshToken = RefreshToken.builder()
        											.user(user)
        											.token(refreshToken)
        											.expiryDate(expiryDate)
        											.build();
        refreshTokenRepository.save(savedRefreshToken);
        
        return new TokenResponse(TOKEN_TYPE, accessToken, refreshToken);
        
	}

}
