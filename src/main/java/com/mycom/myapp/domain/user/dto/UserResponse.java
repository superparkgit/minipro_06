package com.mycom.myapp.domain.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.mycom.myapp.domain.user.entity.User;

public record UserResponse(
    Long id,
    String email,
    String name,
    List<String> roles,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRoles().stream()
                .map(r -> r.getRoleName().name())
                .collect(Collectors.toList()),
            user.getCreatedAt()
        );
    }
}
