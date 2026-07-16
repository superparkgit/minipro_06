package com.mycom.myapp.domain.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.program.repository.ProgramRepository;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

/**
 * ReservationService - 박서희 담당
 * <p>
 * TODO: 다음 API 구현 (핵심 로직 주의)
 * - createReservation(request, userId): 예약 신청
 *   → 중복 예약 방지 (existsByUserIdAndProgramId)
 *   → 정원 체크 (countByProgramIdAndStatus < capacity)
 *   → 동시성 고려 시 @Lock(PESSIMISTIC_WRITE) 또는 @Version 사용 (가산점)
 * - getMyReservations(userId): 내 예약 목록
 * - cancelReservation(id, userId): 예약 취소 (본인 확인)
 * - getReservationsByProgram(programId): 특정 프로그램 예약자 목록 (TRAINER)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ProgramRepository programRepository;

    // TODO: 박서희 - 서비스 메서드 구현
}
