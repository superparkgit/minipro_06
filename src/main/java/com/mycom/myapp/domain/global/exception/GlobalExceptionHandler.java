package com.mycom.myapp.domain.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mycom.myapp.domain.auth.exception.DuplicateEmailException;

/**
 * Controller와 Service에서 발생한 예외 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
	/**
	 * 이미 사용 중인 이메일로 회원가입 시도한 경우
	 */
	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(DuplicateEmailException exception){
		HttpStatus status = HttpStatus.CONFLICT; // 409 Conflict
		ApiErrorResponse response = new ApiErrorResponse(
				status.value(),
				status.name(),
				exception.getMessage()
		);
		
		return ResponseEntity
					.status(status)
					.body(response);
	}


	/**
	 * 요청한 리소스를 찾을 수 없는 경우 (게시글, 댓글, 사용자, 예약 등)
	 */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException exception) {
		HttpStatus status = HttpStatus.NOT_FOUND; // 404 Not Found
		ApiErrorResponse response = new ApiErrorResponse(
				status.value(),
				status.name(),
				exception.getMessage());

		return ResponseEntity
				.status(status)
				.body(response);
	}

	/**
	 * 요청한 작업에 대한 권한이 없는 경우
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception) {
		HttpStatus status = HttpStatus.FORBIDDEN; // 403 Forbidden
		ApiErrorResponse response = new ApiErrorResponse(
				status.value(),
				status.name(),
				exception.getMessage());

		return ResponseEntity
				.status(status)
				.body(response);
	}

	/**
	 * 비즈니스 규칙을 위반한 경우 (중복 작성, 잘못된 상태 전이 등)
	 */
	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ApiErrorResponse> handleBusinessRule(BusinessRuleException exception) {
		HttpStatus status = HttpStatus.CONFLICT; // 409 Conflict
		ApiErrorResponse response = new ApiErrorResponse(
				status.value(),
				status.name(),
				exception.getMessage());

		return ResponseEntity
				.status(status)
				.body(response);
	}
}
