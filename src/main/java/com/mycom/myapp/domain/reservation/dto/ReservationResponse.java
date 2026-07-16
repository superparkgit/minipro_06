package com.mycom.myapp.domain.reservation.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.entity.Reservation.ReservationStatus;

public record ReservationResponse(
    Long id,
    Long userId,
    Long programId,
    String programName,
    ReservationStatus status,
    LocalDateTime createdAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getUser().getId(),
            reservation.getProgram().getId(),
            reservation.getProgram().getTitle(),
            reservation.getStatus(),
            reservation.getCreatedAt()
        );
    }
}
