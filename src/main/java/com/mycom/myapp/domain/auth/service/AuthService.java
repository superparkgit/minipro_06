package com.mycom.myapp.domain.auth.service;

import com.mycom.myapp.domain.auth.dto.AccessTokenResponse;
import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.RefreshTokenRequest;
import com.mycom.myapp.domain.auth.dto.SignupRequest;
import com.mycom.myapp.domain.auth.dto.SignupResponse;
import com.mycom.myapp.domain.auth.dto.TokenResponse;

/**
 * 회원가입과 로그인 정의
 */
public interface AuthService {

	/**
	 * 회원가입 
	 */
	SignupResponse signup(SignupRequest request);

	/**
	 * 로그인
	 */
	TokenResponse login(LoginRequest request);
	
	/**
	 * Refresh Token 검증하고 새로운 Access Token 발급
	 */
	AccessTokenResponse refresh(RefreshTokenRequest request);
	
	/**
	 * Refresh Token 폐기하고 로그아웃.
	 */
	void logout(RefreshTokenRequest request);
}
