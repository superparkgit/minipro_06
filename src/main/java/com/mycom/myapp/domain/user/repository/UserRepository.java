package com.mycom.myapp.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);

	@Query("""
			select distinct u
			from User u
			join u.userRoles ur
			where ur.roleName = :role
			order by u.name
			""")
	List<User> findAllByRole(@Param("role") Role role);
}
