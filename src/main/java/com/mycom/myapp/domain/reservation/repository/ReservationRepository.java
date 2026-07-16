package com.mycom.myapp.domain.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // TODO: 박서희 - 예약 관련 쿼리 메서드 추가
    // - findByUserIdAndStatus(userId, status): 내 예약 목록
    // - countByGymClassIdAndStatus(gymClassId, status): 현재 예약 인원 수 (정원 체크)
    // - existsByUserIdAndGymClassId(userId, gymClassId): 중복 예약 확인
    // - findByGymClassId(gymClassId): 특정 수업의 예약자 목록
}
