package com.mycom.myapp.domain.auth.dto;

/**
 * Refresh Token 검증 성공 후 새로 발급한 Access Token 응답.
 */
public record AccessTokenResponse(
		String tokenType, // Bearer
		String accessToken
){

}
