package com.mycom.myapp.domain.user.dto.response;

import java.util.Set;
import java.util.stream.Collectors;

import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.entity.UserRole;

/**
 * 로그인한 회원의 기본 정보와 역할 목록을 전달하는 응답 DTO.
 */
public record MyProfileResponse(
        Long id,
        String email,
        String name,
        Set<Role> roles
) {

    /**
     * User 엔티티를 내 정보 조회 응답으로 변환.
     */
    public static MyProfileResponse from(User user) {
        Set<Role> roles = user.getUserRoles().stream()
                .map(UserRole::getRoleName)
                .collect(Collectors.toSet());

        return new MyProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                roles
        );
    }
}
