package com.mycom.myapp.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Access Token 재발급과 로그아웃 시 사용하는 Refresh Token 요청.
 */
public record RefreshTokenRequest(
		
		@NotBlank(message = "Refresh Token은 필수입니다.")
		String refreshToken
){

}
