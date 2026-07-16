package com.mycom.myapp.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.user.entity.UserRole;
import com.mycom.myapp.domain.user.entity.UserRole.RoleName;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    Optional<UserRole> findByRoleName(RoleName roleName);
}
