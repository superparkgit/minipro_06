package com.mycom.myapp.domain.user.dto.request;

import java.util.Set;

import com.mycom.myapp.domain.user.entity.Role;

import jakarta.validation.constraints.NotEmpty;

/**
 * 관리자 회원 역할 변경 요청
 */
public record UpdateUserRolesRequest(
		
		@NotEmpty(message = "역할을 하나 이상 선택해야 합니다.")
		Set<Role> roles
) {
}
