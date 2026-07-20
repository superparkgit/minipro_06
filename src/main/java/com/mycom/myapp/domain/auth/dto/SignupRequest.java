package com.mycom.myapp.domain.auth.dto;

/**
 * 회원가입 API에서 전달받는 요청 데이터.
 * 일반 회원가입 시 User 엔티티에서 USER 역할이 기본 설정.
 */
public record SignupRequest(
		String email,
		String password,
		String name
) {

}
