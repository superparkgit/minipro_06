package com.mycom.myapp.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.SignupRequest;
import com.mycom.myapp.domain.auth.dto.SignupResponse;
import com.mycom.myapp.domain.auth.dto.TokenResponse;
import com.mycom.myapp.domain.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	
	/**
	 * 회원가입
	 */
	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request){
		SignupResponse signupResponse = authService.signup(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);
	}
	
	/**
	 * 로그인 및 JWT 발급
	 */
	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request){
		TokenResponse tokenResponse = authService.login(request);
		return ResponseEntity.ok(tokenResponse);
	}
}
