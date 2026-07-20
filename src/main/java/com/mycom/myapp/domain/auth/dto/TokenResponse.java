package com.mycom.myapp.domain.auth.dto;

/**
 * 로그인 성공 시 프론트에 반환하는 JWT 응답
 */
public record TokenResponse(
		String tokenType,
		String accessToken,
		String refreshToken
){

}
