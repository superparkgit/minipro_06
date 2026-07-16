package com.mycom.myapp.domain.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mycom.myapp.domain.user.entity.Role;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CustomUserDetails implements UserDetails {
	
	private static final long serialVersionUID = 1L;
	
	// 로그인 한 회원의 PK
	private final Long userId;
	
	// 로그인 식별자로 사용하는 email
	private final String username;
	
	// DB에 BCrypt 방식으로 암호화되어 저장된 비밀번호
	private final String password;
	
	// 회원 이름
	private final String name;
	
	// USER, TRAINER, ADMIN
	private final Role role;
	
	// ROLE_USER, ROLE_TRAINER, ROLE_ADMIN 형태의 권한 목록
	// 권한 변환은 CustomUserDetailsService에서 수행.
	private final Collection<? extends GrantedAuthority> authorities;


}
