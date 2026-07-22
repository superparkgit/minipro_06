package com.mycom.myapp.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
	Optional<UserRole> findByRoleName(Role roleName);
}
