package com.mycom.myapp.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.security.CustomUserDetails;
import com.mycom.myapp.domain.user.dto.response.MyProfileResponse;
import com.mycom.myapp.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인한 회원의 기본 정보와 역할 목록을 조회.
     */
    @GetMapping("/me")
    public ResponseEntity<MyProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyProfileResponse response = userService.getMyProfile(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }
}
