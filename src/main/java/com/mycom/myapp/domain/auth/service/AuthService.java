package com.mycom.myapp.domain.auth.service;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.SignupRequest;
import com.mycom.myapp.domain.auth.dto.TokenResponse;

/**
 * 회원가입과 로그인 정의
 * 
 * 추후 기능이 늘어나면 분리 
 * SignupService 회원가입, 이메일 인증
 * LoginService 로그인, 로그아웃
 * TokenService 토큰 발급, 재발급, 폐기
 * AdminUserService 별도 관리자
 */
public interface AuthService {

	void signup(SignupRequest request);

	TokenResponse login(LoginRequest request);
}
