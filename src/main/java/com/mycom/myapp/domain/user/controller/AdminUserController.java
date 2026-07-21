package com.mycom.myapp.domain.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.user.dto.request.UpdateUserRolesRequest;
import com.mycom.myapp.domain.user.dto.response.AdminUserResponse;
import com.mycom.myapp.domain.user.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 회원관리
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
	private final AdminUserService adminUserService;
	
	@GetMapping
	public ResponseEntity<List<AdminUserResponse>> getUsers(){
		List<AdminUserResponse> users = adminUserService.getUsers();
		return ResponseEntity.ok(users);
	}
	@PatchMapping("/{userId}/roles")
	public ResponseEntity<AdminUserResponse> updateRoles(
			@PathVariable("userId") Long userId,
			@Valid @RequestBody UpdateUserRolesRequest request
	){
		AdminUserResponse adminUserResponse = adminUserService.updateRoles(userId, request);
		return ResponseEntity.ok(adminUserResponse);
	}
	
}
