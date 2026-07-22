package com.mycom.myapp.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mycom.myapp.domain.user.dto.request.UpdateUserRolesRequest;
import com.mycom.myapp.domain.user.dto.response.AdminUserResponse;
import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.entity.UserRole;
import com.mycom.myapp.domain.user.repository.UserRepository;
import com.mycom.myapp.domain.user.repository.UserRoleRepository;

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceImplTest {
	
	@Mock
	private UserRepository userRepository;

	@Mock
	private UserRoleRepository userRoleRepository;
	
	@InjectMocks
	private AdminUserServiceImpl adminUserService;
	
	/**
	 * UserRole Mock 객체
	 */
    private UserRole role(Role role) {
        UserRole userRole = org.mockito.Mockito.mock(UserRole.class);

        given(userRole.getRoleName()).willReturn(role);

        return userRole;
    }
	
    /**
     * User 객체
     */
    private User user(Long id, Set<UserRole> roles) {
    	User user = User.builder()
    					.email("user@test.com")
    					.password("password")
    					.name("테스트")
    					.userRoles(roles)
    					.build();
    	ReflectionTestUtils.setField(user, "id", id);
    	ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.of(2026, 7, 21, 12, 0));
    	
    	return user;
    }
    
	@Test
	@DisplayName("전체 회원 목록을 조회")
	void getUser() {
		UserRole userRole = role(Role.ROLE_USER);
		User user = user(1L, Set.of(userRole));
		
		given(userRepository.findAll()).willReturn(List.of(user));
		
		List<AdminUserResponse> result = adminUserService.getUsers();
		
		assertThat(result).hasSize(1);
		assertThat(result.get(0).id()).isEqualTo(1L);
		assertThat(result.get(0).email()).isEqualTo("user@test.com");
		assertThat(result.get(0).roles()).containsExactly(Role.ROLE_USER);
		verify(userRepository).findAll();
	}
	
	@Test
	@DisplayName("회원 역할 변경")
	void updateRoles() {
		UserRole userRole = role(Role.ROLE_USER);
		UserRole trainerRole = role(Role.ROLE_TRAINER);
		User user = user(1L, Set.of(userRole));
		
		UpdateUserRolesRequest request = new UpdateUserRolesRequest(
												Set.of(Role.ROLE_USER,Role.ROLE_TRAINER)
												);
		given(userRepository.findById(1L)).willReturn(Optional.of(user));
		given(userRoleRepository.findByRoleName(Role.ROLE_USER)).willReturn(Optional.of(userRole));
		given(userRoleRepository.findByRoleName(Role.ROLE_TRAINER)).willReturn(Optional.of(trainerRole));
		
		AdminUserResponse result = adminUserService.updateRoles(1L, request);
		
		assertThat(result.roles()).containsExactlyInAnyOrder(Role.ROLE_USER,Role.ROLE_TRAINER);
		
		assertThat(user.getUserRoles()).containsExactlyInAnyOrder(userRole, trainerRole);
	}
}

