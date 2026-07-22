package com.mycom.myapp.domain.security;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.entity.UserRole;
import com.mycom.myapp.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

// Spring Security가 로그인 사용자를 조회할 때 사용하는 서비스.
/*
 * 1. 로그인 화면에서 입력받은 이메일로 User 조회.
 * 2. 조회한 User를 CustomUserDetails로 변환.
 * 3. 변환한 CustomUserDetails를 Spring Security에 반환.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{
	
	private final UserRepository userRepository;
	
	/**
	 * @param email 사용자가 로그인할 때 입력한 이메일
	 * @return 사용할 인증 사용자 정보
	 * @throws UsernameNotFoundException 이메일에 해당하는 회원이 없을 때
	 */
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		// 사용자가 입력한 이메일로 회원 조회.
		Optional<User> optionalUser = userRepository.findByEmail(email);
		
		if(optionalUser.isPresent()) {
			User user = optionalUser.get();
			
			/**
			 * UserRole 엔티티 목록에서 Role enum 목록을 생성
			 */
	        Set<Role> roles = user.getUserRoles().stream()
	                .map(UserRole::getRoleName)
	                .collect(Collectors.toSet());
			
	        /**
	         * DB 값에 ROLE_ 이 이미 있으므로 prefix를 추가하지 않음.
	         */
			List<SimpleGrantedAuthority> authorities =
					roles.stream()
						.map(Role::name)
						.map(SimpleGrantedAuthority::new)
						.toList();
	        
			return CustomUserDetails.builder()
						.username(user.getEmail()) // Spring Security 계약 필드
						.password(user.getPassword()) // BCrypt 암호화 비밀번호

						.userId(user.getId()) // 추가 정보
						.name(user.getName())
						.roles(roles)
						.authorities(authorities)
						.build();
		}
		
		throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
	}
}
