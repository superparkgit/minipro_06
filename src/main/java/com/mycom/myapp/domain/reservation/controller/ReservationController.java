package com.mycom.myapp.domain.reservation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.reservation.dto.ReservationRequest;
import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.domain.reservation.dto.AttendanceRequest;
import com.mycom.myapp.domain.reservation.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 신청
    @PostMapping("/api/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal Long userId) {
        ReservationResponse response =
                reservationService.createReservation(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 내 예약 목록
    @GetMapping("/api/reservations/me")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(reservationService.getMyReservations(userId));
    }

    // 예약 취소 (본인만)
    @PatchMapping("/api/reservations/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        reservationService.cancelReservation(id, userId);
        return ResponseEntity.noContent().build();
    }

    // 특정 프로그램의 예약자 목록 (TRAINER, 본인 프로그램만)
    // 예약자 개인정보가 노출되므로 담당 트레이너 본인만 조회 가능
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/api/programs/{id}/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservationsByProgram(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(reservationService.getReservationsByProgram(id, userId));
    }

    // 예약 승인 (TRAINER, 본인 프로그램만)
    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/api/reservations/{id}/approve")
    public ResponseEntity<ReservationResponse> approveReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(reservationService.approveReservation(id, userId));
    }

    // 예약 거절 (TRAINER, 본인 프로그램만)
    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/api/reservations/{id}/reject")
    public ResponseEntity<ReservationResponse> rejectReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(reservationService.rejectReservation(id, userId));
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/api/reservations/{id}/attendance")
    public ResponseEntity<ReservationResponse> markAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(reservationService.markAttendance(id, userId, request));
    }
}
