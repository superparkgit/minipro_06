package com.mycom.myapp.domain.auth.exception;

/**
 * Controller 에서 발생한 예외를 프론트에 전달하는 응답 DTO
 */
public record ApiErrorResponse(
		int status,
		String error,
		String message
) {
}
