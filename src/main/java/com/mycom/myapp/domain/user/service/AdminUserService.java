package com.mycom.myapp.domain.user.service;

import java.util.List;

import com.mycom.myapp.domain.user.dto.request.UpdateUserRolesRequest;
import com.mycom.myapp.domain.user.dto.response.AdminUserResponse;

/**
 * 관리자 회원관리 기능 정의
 */
public interface AdminUserService {
	
	// 전체 회원 목록 조회 추후
	// 추후 회원이 많아진다고 가정하면 Page
	List<AdminUserResponse> getUsers();
	
	// 특정 회원 역할 변경
	AdminUserResponse updateRoles(Long userId, UpdateUserRolesRequest request);
}
