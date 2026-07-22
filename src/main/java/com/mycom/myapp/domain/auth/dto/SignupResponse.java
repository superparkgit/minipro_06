package com.mycom.myapp.domain.auth.dto;

/**
 * 회원가입 성공 응답.
 */
public record SignupResponse(
		Long userId,
		String email,
		String name
) {
}
