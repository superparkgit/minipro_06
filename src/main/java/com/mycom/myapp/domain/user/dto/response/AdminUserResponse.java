package com.mycom.myapp.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.entity.UserRole;

/**
 * 관리자 회원 조회 응답
 */
public record AdminUserResponse(
	Long id,
	String email,
	String name,
	Set<Role> roles,
	LocalDateTime createdAt
) {

	/**
	 * User 엔티티를 관리자 회원으로 응답 변환
	 */
	public static AdminUserResponse from(User user) {
		Set<Role> roles = user.getUserRoles().stream()
								.map(UserRole::getRoleName)
								.collect(Collectors.toSet());
		return new AdminUserResponse(
				user.getId(),
				user.getEmail(),
				user.getName(),
				roles,
				user.getCreatedAt());
				
	}
	
}
