package com.mycom.myapp.domain.reservation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.program.dto.ProgramResponse;
import com.mycom.myapp.domain.reservation.dto.ProgramStatsResponse;
import com.mycom.myapp.domain.reservation.dto.TrainerStatsResponse;
import com.mycom.myapp.domain.reservation.service.ReservationService;

import lombok.RequiredArgsConstructor;

/**
 * 예약 통계 API.
 * JOIN / GROUP BY / 서브쿼리 연습 포인트를 API 로 노출.
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ReservationService reservationService;

    // 예약이 한 건도 없는 프로그램 목록 (NOT EXISTS 서브쿼리)
    @GetMapping("/programs/empty")
    public ResponseEntity<List<ProgramResponse>> getProgramsWithoutReservation() {
        return ResponseEntity.ok(reservationService.getProgramsWithoutReservation());
    }

    // 트레이너별 이번 달 승인된 예약 건수 (JOIN + GROUP BY)
    @GetMapping("/trainers/monthly")
    public ResponseEntity<List<TrainerStatsResponse>> getMonthlyApprovedCountByTrainer() {
        return ResponseEntity.ok(reservationService.getMonthlyApprovedCountByTrainer());
    }

    // 승인 건수 기준 인기 프로그램 TOP N (기본 3개)
    @GetMapping("/programs/popular")
    public ResponseEntity<List<ProgramStatsResponse>> getPopularPrograms(
            @RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok(reservationService.getPopularPrograms(limit));
    }
}
