package com.mycom.myapp.domain.auth.exception;

/**
 * Refresh Token이 유효하지 않을 때 발생.
 */
public class InvalidRefreshTokenException extends RuntimeException{
	public InvalidRefreshTokenException() {
		super("유효하지 않은 Refresh Token 입니다.");
	}
}
