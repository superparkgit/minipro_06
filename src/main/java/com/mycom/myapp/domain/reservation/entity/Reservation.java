package com.mycom.myapp.domain.reservation.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation")
// 중복 예약 방지는 서비스 계층에서 체크 (취소/거절 후 재신청이 가능해야 하므로 DB 유니크 제약은 사용하지 않음)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;  // 신청 시 대기 상태

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 승인 (TRAINER)
    public void approve() {
        this.status = ReservationStatus.APPROVED;
    }

    // 거절 (TRAINER)
    public void reject() {
        this.status = ReservationStatus.REJECTED;
    }

    // 취소 (본인)
    public void cancel() {
        this.status = ReservationStatus.CANCELED;
    }

    public boolean isPending() {
        return this.status == ReservationStatus.PENDING;
    }

    public boolean isCanceled() {
        return this.status == ReservationStatus.CANCELED;
    }

    public enum ReservationStatus {
        PENDING,   // 대기 (신청 직후)
        APPROVED,  // 승인
        REJECTED,  // 거절
        CANCELED   // 취소
    }
}
