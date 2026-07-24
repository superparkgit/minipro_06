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
    LocalDateTime createdAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getUser().getId(),
            reservation.getUser().getName(),
            reservation.getProgram().getId(),
            reservation.getProgram().getTitle(),
            reservation.getStatus(),
            reservation.getAttendanceStatus(),
            reservation.getCreatedAt()
        );
    }
}
