package com.mycom.myapp.domain.reservation.dto;

import com.mycom.myapp.domain.reservation.entity.Reservation.AttendanceStatus;

import jakarta.validation.constraints.NotNull;

public record AttendanceRequest(
        @NotNull(message = "출석 상태는 필수입니다.") AttendanceStatus attendanceStatus
) {}
