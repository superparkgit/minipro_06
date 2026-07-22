package com.mycom.myapp.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.TokenResponse;
import com.mycom.myapp.domain.auth.entity.RefreshToken;
import com.mycom.myapp.domain.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.domain.security.CustomUserDetails;
import com.mycom.myapp.domain.security.jwt.JwtUtil;
import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;
import com.mycom.myapp.domain.user.repository.UserRoleRepository;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
public class AuthServiceLoginTest {

	
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
    
	private static final Long USER_ID = 1L;
	private static final String EMAIL = "test@test.com";
	private static final String PASSWORD = "password";
	private static final String WRONG_PASSWORD = "wrong-password";
	private static final String ENCODED_PASSWORD = "encoded-password";
	private static final String NAME = "테스트";
	private static final String ACCESS_TOKEN = "access-token";
	private static final String REFRESH_TOKEN = "refresh-token";
    
    private LoginRequest createLoginRequest(String password) {
    	return new LoginRequest(EMAIL, password);
    }
	
    private CustomUserDetails createUserDetails() {
    	return CustomUserDetails.builder()
    							.userId(USER_ID)
    							.username(EMAIL)
    							.password(ENCODED_PASSWORD)
    							.roles(Set.of(Role.ROLE_USER))
    							.authorities(Set.of())
    							.build();
    }
    
	@Test
	@DisplayName("로그인에 성공하면 Access/Refresh Token 발급.")
	void login_success() {
 
		
        LoginRequest request = createLoginRequest(PASSWORD);


        CustomUserDetails userDetails = createUserDetails();

        Authentication authentication = mock(Authentication.class);

        given(authentication.getPrincipal())
                .willReturn(userDetails);

        given(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).willReturn(authentication);

        given(jwtUtil.createAccessToken(request.email())).willReturn(ACCESS_TOKEN);

        given(jwtUtil.createRefreshToken(request.email())).willReturn(REFRESH_TOKEN);

        Instant expiration = Instant.parse("2026-08-04T02:00:00Z");

        Claims claims = mock(Claims.class);

        given(claims.getExpiration()).willReturn(Date.from(expiration));

        given(jwtUtil.validateRefreshToken(REFRESH_TOKEN)).willReturn(claims);

        User user = User.builder()
                .email(request.email())
                .password(ENCODED_PASSWORD)
                .name(NAME)
                .build();

        given(userRepository.getReferenceById(1L))
                .willReturn(user);

        TokenResponse response = authService.login(request);

        assertThat(response.tokenType()).isEqualTo("Bearer");

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);

        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);

        ArgumentCaptor<UsernamePasswordAuthenticationToken>
                authenticationCaptor =
                ArgumentCaptor.forClass(
                        UsernamePasswordAuthenticationToken.class
                );

        verify(authenticationManager).authenticate(authenticationCaptor.capture());

        UsernamePasswordAuthenticationToken authenticationRequest =authenticationCaptor.getValue();

        assertThat(authenticationRequest.getPrincipal()).isEqualTo(request.email());

        assertThat(authenticationRequest.getCredentials()).isEqualTo(request.password());

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();

        assertThat(savedRefreshToken.getToken()).isEqualTo(REFRESH_TOKEN);

        assertThat(savedRefreshToken.getUser()).isSameAs(user);

        LocalDateTime expectedExpiresAt =
                LocalDateTime.ofInstant(
                        expiration,
                        ZoneId.systemDefault()
                );

        assertThat(savedRefreshToken.getExpiresAt()).isEqualTo(expectedExpiresAt);
	}
	
    @Test
    @DisplayName("이메일 또는 비밀번호가 틀리면 로그인에 실패")
    void login_badCredentials() {

        LoginRequest request = createLoginRequest(WRONG_PASSWORD);

        given(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).willThrow(
                new BadCredentialsException(
                        "이메일 또는 비밀번호가 올바르지 않습니다."
                )
        );

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("이메일 또는 비밀번호");

        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(refreshTokenRepository);
    }
    
}
