package com.mycom.myapp.domain.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
	 * @Valid 검증에 실패한 경우 처리.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception){
		HttpStatus status = HttpStatus.BAD_REQUEST; // 400 Bad Request
		
		String message = exception.getBindingResult()
							.getFieldErrors()
							.stream()
							.findFirst()
							.map(fieldError -> fieldError.getDefaultMessage())
							.orElse("요청 값이 올바르지 않습니다.");
		
		ApiErrorResponse response = new ApiErrorResponse(
				status.value(),
				status.name(),
				message
		);
		
		return ResponseEntity
					.status(status)
					.body(response);
	}
}
