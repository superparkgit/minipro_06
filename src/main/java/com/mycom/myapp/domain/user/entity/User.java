package com.mycom.myapp.domain.user.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String name;

    /**
     * User가 관계의 주인.
     * user_user_roles 연결 테이블도 함께 관리.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
    		name = "user_user_roles",
    		joinColumns = @JoinColumn(name = "user_id"),
    		inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<UserRole> userRoles = new HashSet<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    private User(String email, String password, String name, Set<UserRole> userRoles) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.userRoles = userRoles == null ? new HashSet<>() : new HashSet<>(userRoles);
    }
}
