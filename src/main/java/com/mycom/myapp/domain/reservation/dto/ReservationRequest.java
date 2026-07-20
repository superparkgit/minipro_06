package com.mycom.myapp.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
    @NotNull(message = "프로그램 ID는 필수입니다.")
    Long programId
) {}
