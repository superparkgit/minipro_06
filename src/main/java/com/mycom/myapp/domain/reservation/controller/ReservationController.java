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
import com.mycom.myapp.domain.reservation.service.ReservationService;
import com.mycom.myapp.security.CustomUserDetails;

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
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReservationResponse response =
                reservationService.createReservation(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 내 예약 목록
    @GetMapping("/api/reservations/me")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.getMyReservations(userDetails.getUserId()));
    }

    // 예약 취소 (본인만)
    @PatchMapping("/api/reservations/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reservationService.cancelReservation(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    // 특정 프로그램의 예약자 목록 (TRAINER)
    @GetMapping("/api/programs/{id}/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservationsByProgram(
            @PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationsByProgram(id));
    }

    // 예약 승인 (TRAINER, 본인 프로그램만)
    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/api/reservations/{id}/approve")
    public ResponseEntity<ReservationResponse> approveReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.approveReservation(id, userDetails.getUserId()));
    }

    // 예약 거절 (TRAINER, 본인 프로그램만)
    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/api/reservations/{id}/reject")
    public ResponseEntity<ReservationResponse> rejectReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(reservationService.rejectReservation(id, userDetails.getUserId()));
    }
}