package com.mycom.myapp.domain.auth.exception;

/**
 * 이미 가입된 이메일로 회원가입을 시도했을 때 발생.
 */
public class DuplicateEmailException extends RuntimeException {

	public DuplicateEmailException() {
		super("이미 사용 중인 이메일입니다.");
	}

}
