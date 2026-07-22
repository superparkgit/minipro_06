package com.mycom.myapp.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mycom.myapp.domain.auth.dto.AccessTokenResponse;
import com.mycom.myapp.domain.auth.dto.RefreshTokenRequest;
import com.mycom.myapp.domain.auth.entity.RefreshToken;
import com.mycom.myapp.domain.auth.exception.InvalidRefreshTokenException;
import com.mycom.myapp.domain.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.domain.security.jwt.JwtUtil;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;
import com.mycom.myapp.domain.user.repository.UserRoleRepository;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTokenTest {

	
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    
    private static final String EMAIL = "test@test.com";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String NAME = "테스트";
    
    private User createUser() {
    	return User.builder()
    				.email(EMAIL)
    				.password("encoded-password")
    				.name(NAME)
    				.build();
    }
    
    private RefreshToken createSavedRefreshToken(User user) {
    	return RefreshToken.builder()
    					.user(user)
    					.token(REFRESH_TOKEN)
    					.expiresAt(LocalDateTime.now().plusDays(14))
    					.build();
    }
    
    
    @Test
    @DisplayName("유효한 Refresh Token이면 새로운 AccessToken을 발급")
    void refresh_success() {
    	// AuthServiceImpl ->
    		// AccessTokenResponse refresh(RefreshTokenRequest request)
    	
    	// JWT 서명, 만료시간, REFRESH 인지 검증.
    	RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
    	
    	Claims claims = mock(Claims.class);
    	
    	given(jwtUtil.validateRefreshToken(request.refreshToken())).willReturn(claims);
    	
    	// JWT에 저장된 이메일과 토큰 소유가 이메일이 같은지 확인.
    	
    	given(claims.getSubject()).willReturn(EMAIL);
    	
    	User user = createUser();
    	// 로그인 할 때 DB에 저장했던 Refresh token 확인.
    	RefreshToken savedRefreshToken = createSavedRefreshToken(user);
    	given(refreshTokenRepository.findByToken(request.refreshToken()))
    								.willReturn(Optional.of(savedRefreshToken));
    	
    	// 재발급
    	given(jwtUtil.createAccessToken(EMAIL)).willReturn("new-access-token");
    	
    	AccessTokenResponse response = authService.refresh(request);
    	
    	assertThat(response.tokenType()).isEqualTo("Bearer");
    	assertThat(response.accessToken()).isEqualTo("new-access-token");
    	
    	verify(jwtUtil).validateRefreshToken(request.refreshToken());
    	verify(refreshTokenRepository).findByToken(request.refreshToken());
    	verify(jwtUtil).createAccessToken(EMAIL);
    }
    
    @Test
    @DisplayName("유효하지 않은 Refresh Token 이면 재발급에 실패")
 // if(claims == null) throw new InvalidRefreshTokenException();
    void refresh_invalidToken(){
    	RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");
    	
    	given(jwtUtil.validateRefreshToken(request.refreshToken())).willReturn(null);
    	
    	// 서비스 실행하면 InvalidRefreshTokenException 발생
    	assertThatThrownBy(() -> authService.refresh(request))
    							.isInstanceOf(InvalidRefreshTokenException.class)
    							.hasMessageContaining("Refresh Token");
    	
    	// JWT가 유효하지 않으므로 DB 조회 X
    	verifyNoInteractions(refreshTokenRepository);
    	
    	verify(jwtUtil,never()).createAccessToken(anyString());
    }
    
    @Test
    @DisplayName("JWT는 유효 하지만 DB에 저장되지 않은 RefreshToken면 재발급 실패")
    void refresh_tokenNotStored() {
    	RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
    	Claims claims = mock(Claims.class);
    	
    	given(jwtUtil.validateRefreshToken(request.refreshToken())).willReturn(claims);
    	
    	given(refreshTokenRepository.findByToken(request.refreshToken()))
				.willReturn(Optional.empty());
    	
    	assertThatThrownBy(() -> authService.refresh(request))
				.isInstanceOf(InvalidRefreshTokenException.class);
    	
    	verify(refreshTokenRepository).findByToken(request.refreshToken());
    	
    	verify(jwtUtil, never()).createAccessToken(anyString());
    }
    
    @Test
    @DisplayName("로그아웃하면 DB에 저장된 Refresh Token 삭제")
    void logout_success() {
    	RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
    	
    	Claims claims = mock(Claims.class);
    	given(jwtUtil.validateRefreshToken(request.refreshToken())).willReturn(claims);
    	
    	given(claims.getSubject()).willReturn(EMAIL);
    	
    	User user = createUser();
    	RefreshToken savedRefreshToken = createSavedRefreshToken(user);
    	given(refreshTokenRepository.findByToken(request.refreshToken()))
    								.willReturn(Optional.of(savedRefreshToken));
    	
    	authService.logout(request);
    	verify(refreshTokenRepository).delete(savedRefreshToken);
    }
}
