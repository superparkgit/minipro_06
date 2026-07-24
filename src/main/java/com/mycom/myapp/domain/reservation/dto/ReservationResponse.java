package com.mycom.myapp.domain.reservation.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.entity.Reservation.ReservationStatus;
import com.mycom.myapp.domain.reservation.entity.Reservation.AttendanceStatus;

public record ReservationResponse(
    Long id,
    Long userId,
    String userName,
    Long programId,
    String programName,
    ReservationStatus status,
    AttendanceStatus attendanceStatus,
    LocalDateTime createdAt,
    Long reviewId,
    Integer reviewRating,
    String reviewContent
) {
    public static ReservationResponse from(Reservation reservation) {
        return from(reservation, null, null, null);
    }

    public static ReservationResponse from(
            Reservation reservation, Long reviewId, Integer reviewRating, String reviewContent) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getUser().getId(),
            reservation.getUser().getName(),
            reservation.getProgram().getId(),
            reservation.getProgram().getTitle(),
            reservation.getStatus(),
            reservation.getAttendanceStatus(),
            reservation.getCreatedAt(),
            reviewId,
            reviewRating,
            reviewContent
        );
    }
}
