package com.mycom.myapp.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.mycom.myapp.domain.auth.dto.SignupRequest;
import com.mycom.myapp.domain.auth.dto.SignupResponse;
import com.mycom.myapp.domain.auth.exception.DuplicateEmailException;
import com.mycom.myapp.domain.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.domain.security.jwt.JwtUtil;
import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.entity.UserRole;
import com.mycom.myapp.domain.user.repository.UserRepository;
import com.mycom.myapp.domain.user.repository.UserRoleRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceSignupTest {
	
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
	
	/**
	 * 위의 Mock 객체들을 주입하여
	 * 테스트 대상인 AuthServiceImpl 생성
	 */
	@InjectMocks
	private AuthServiceImpl authService;
	
	private static final String EMAIL = "test@test.com";
	private static final String PASSWORD = "password";
	private static final String NAME = "테스트";
	
	private SignupRequest createSignupRequest() {
		return new SignupRequest(EMAIL, PASSWORD, NAME);
	}
	
	
	@Test
	@DisplayName("회원가입 성공 테스트")
	void signup_success() {
		
		
		SignupRequest request = createSignupRequest();
		
		UserRole defaultRole = mock(UserRole.class);
	
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        
        given(userRoleRepository.findByRoleName(Role.ROLE_USER)).willReturn(Optional.of(defaultRole));
        
        given(passwordEncoder.encode(request.password())).willReturn("encoded-password");
        
        given(userRepository.save(any(User.class)))
        .willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);

            return user;
        });
        
        SignupResponse response = authService.signup(request);
        
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.name()).isEqualTo(request.name());
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        
        assertThat(savedUser.getUserRoles()).containsExactly(defaultRole);
	}
	
    @Test
    @DisplayName("사용 중인 이메일이면 회원가입에 실패")
    void signup_duplicateEmail() {

        SignupRequest request = createSignupRequest();

        given(userRepository.existsByEmail(request.email()))
                .willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("이미 사용 중인 이메일");
     
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(userRoleRepository);

        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("기본 ROLE_USER가 존재하지 않으면 회원가입 실패.")
    void signup_defaultRoleNotFound() {
    	SignupRequest request = createSignupRequest();
    	
    	given(userRepository.existsByEmail(request.email())).willReturn(false);
    	
    	given(userRoleRepository.findByRoleName(Role.ROLE_USER)).willReturn(Optional.empty());
    
    	assertThatThrownBy(() -> authService.signup(request))
    							.isInstanceOf(IllegalStateException.class)
    							.hasMessageContaining("ROLE_USER");
    
    	verifyNoInteractions(passwordEncoder);
    	verify(userRepository, never()).save(any(User.class));
    }
    
}
