package com.mycom.myapp.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.user.dto.UpdateUserRequest;
import com.mycom.myapp.domain.user.dto.UserResponse;
import com.mycom.myapp.domain.user.service.UserService;
import com.mycom.myapp.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** GET /api/users/me */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
        @AuthenticationPrincipal Long userId) {
        UserResponse response = userService.getMe(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** PATCH /api/users/me */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
        @AuthenticationPrincipal Long userId,
        @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateMe(userId, request);
        return ResponseEntity.ok(ApiResponse.success("정보가 수정되었습니다.", response));
    }

    /** POST /api/users/{id}/roles - ADMIN 전용 */
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRole(
        @PathVariable Long id,
        @RequestParam String roleName) {
        UserResponse response = userService.assignRole(id, roleName);
        return ResponseEntity.ok(ApiResponse.success("역할이 부여되었습니다.", response));
    }
}
