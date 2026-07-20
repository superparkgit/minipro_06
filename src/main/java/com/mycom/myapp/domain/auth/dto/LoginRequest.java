package com.mycom.myapp.domain.auth.dto;

/**
 * 로그인 API에서 전달받는 요청 데이터. 
 * AuthenticationManager에 전달.
 */
public record LoginRequest(
		String email,
		String password
) {

}
