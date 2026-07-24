package com.mycom.myapp.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 API에서 전달받는 요청 데이터.
 * 일반 회원가입 시 User 엔티티에서 USER 역할이 기본 설정.
 */
public record SignupRequest(
		
		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		@Pattern(
			    regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
			    message = "이메일 형식이 올바르지 않습니다."		
		)
		String email,
		@NotBlank(message = "비밀번호는 필수입니다.")
		@Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
		String password,
		@NotBlank(message = "이름은 필수입니다.")
		@Size(max = 30, message = "이름은 30자 이하여야 합니다.")
		String name
) {

}
