package com.mycom.myapp.domain.user.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.global.exception.ResourceNotFoundException;
import com.mycom.myapp.domain.user.dto.request.UpdateUserRolesRequest;
import com.mycom.myapp.domain.user.dto.response.AdminUserResponse;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.entity.UserRole;
import com.mycom.myapp.domain.user.repository.UserRepository;
import com.mycom.myapp.domain.user.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	
	/**
	 * 전체 회원 목록 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public List<AdminUserResponse> getUsers() {
		return userRepository.findAll().stream()
							.map(AdminUserResponse::from)
							.toList();
	}

	/**
	 * 특정 회원의 역할 전체 변경
	 * User 조회 -> ROLE_* UserRole 엔티티 조회 -> Set<UserRole> 생성
	 */
	@Override
	@Transactional
	public AdminUserResponse updateRoles(Long userId, UpdateUserRolesRequest request) {
		
		// 전달받은 userId에 해당하는 회원 조회.
		User user = userRepository.findById(userId)
						.orElseThrow(() ->
							new ResourceNotFoundException("회원", userId)
						);
		
		// Role enum 목록을 DB에 저장된 UserRole 엔티티 목록으로 변환.
		Set<UserRole> newRoles = request.roles().stream()
									.map(role -> userRoleRepository.findByRoleName(role)
									.orElseThrow(() -> 
										new IllegalStateException(role + "역할이 존재하지 않습니다.")
									)
								).collect(Collectors.toSet());
									
		// 기존 역할 제거하고 새 역할로 변경.
		user.changeRoles(newRoles);
		
		return AdminUserResponse.from(user);
	}
	
}
