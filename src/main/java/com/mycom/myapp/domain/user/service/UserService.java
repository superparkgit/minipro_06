package com.mycom.myapp.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.user.dto.LoginRequest;
import com.mycom.myapp.domain.user.dto.LoginResponse;
import com.mycom.myapp.domain.user.dto.SignupRequest;
import com.mycom.myapp.domain.user.dto.UpdateUserRequest;
import com.mycom.myapp.domain.user.dto.UserResponse;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.entity.UserRole;
import com.mycom.myapp.domain.user.entity.UserRole.RoleName;
import com.mycom.myapp.domain.user.repository.UserRepository;
import com.mycom.myapp.domain.user.repository.UserRoleRepository;
import com.mycom.myapp.global.exception.CustomException;
import com.mycom.myapp.global.exception.ErrorCode;
import com.mycom.myapp.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /** 회원가입 - 기본 ROLE_USER 부여 */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        UserRole userRole = userRoleRepository.findByRoleName(RoleName.ROLE_USER)
            .orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND));

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .name(request.name())
            .build();
        user.addRole(userRole);

        return UserResponse.from(userRepository.save(user));
    }

    /** 로그인 - JWT 토큰 발급 */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String token = jwtTokenProvider.generateToken(
            user.getId(),
            user.getRoles().stream()
                .map(r -> r.getRoleName().name())
                .toList()
        );

        return LoginResponse.of(token);
    }

    /** 내 정보 조회 */
    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    /** 내 정보 수정 */
    @Transactional
    public UserResponse updateMe(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(request.name());
        return UserResponse.from(user);
    }

    /** 역할 부여 (ADMIN 전용) */
    @Transactional
    public UserResponse assignRole(Long targetUserId, String roleName) {
        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        RoleName rn;
        try {
            rn = RoleName.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }

        UserRole role = userRoleRepository.findByRoleName(rn)
            .orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND));

        user.addRole(role);
        return UserResponse.from(user);
    }
}
