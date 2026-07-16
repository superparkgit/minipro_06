package com.mycom.myapp.domain.reservation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.reservation.service.ReservationService;

import lombok.RequiredArgsConstructor;

/**
 * ReservationController - 박서희 담당
 * <p>
 * TODO: API 구현
 * - POST   /api/reservations                    : 예약 신청 (정원 체크 필수)
 * - GET    /api/reservations/me                 : 내 예약 목록
 * - PATCH  /api/reservations/{id}/cancel        : 예약 취소 (본인만)
 * - GET    /api/programs/{id}/reservations      : 특정 프로그램의 예약자 목록 (TRAINER)
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // TODO: 박서희 - 컨트롤러 메서드 구현
}
