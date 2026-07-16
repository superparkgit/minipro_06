package com.mycom.myapp.domain.program.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.program.entity.Program.ProgramType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProgramRequest(

    @NotBlank(message = "프로그램 제목은 필수입니다.")
    String title,

    @NotNull(message = "프로그램 유형은 필수입니다.")
    ProgramType type,

    @Min(value = 1, message = "정원은 1명 이상이어야 합니다.")
    int capacity,

    @NotNull(message = "시작 시간은 필수입니다.")
    @Future(message = "시작 시간은 미래여야 합니다.")
    LocalDateTime startAt,

    @NotNull(message = "종료 시간은 필수입니다.")
    LocalDateTime endAt,

    String description
) {}
