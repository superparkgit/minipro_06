package com.mycom.myapp.domain.program.dto;

import jakarta.validation.constraints.NotNull;

public record AddTrainerRequest(
        @NotNull(message = "트레이너 ID는 필수입니다.") Long trainerId
) {}
