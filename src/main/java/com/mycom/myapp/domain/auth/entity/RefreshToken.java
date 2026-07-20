package com.mycom.myapp.domain.auth.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.mycom.myapp.domain.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Access Token 재발급에 사용하는 Refresh Token 저장.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/**
	 * 한 명의 사용자가 여러 기기에서 로그인.
	 * 1:N
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	
	private String token;
	
	/**
	 * Refresh Token 만료 시각.
	 */
	private LocalDateTime expiresAt;
	
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Builder
	public RefreshToken(User user, String token, LocalDateTime expiresAt) {
		this.user = user;
		this.token = token;
		this.expiresAt = expiresAt;
	}
	
	
	
	
}
