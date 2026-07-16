package com.mycom.myapp.domain.program.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;

public record ProgramResponse(
    Long id,
    String title,
    ProgramType type,
    Long trainerId,
    String trainerName,
    int capacity,
    LocalDateTime startAt,
    LocalDateTime endAt,
    String description,
    LocalDateTime createdAt
) {
    public static ProgramResponse from(Program program) {
        return new ProgramResponse(
            program.getId(),
            program.getTitle(),
            program.getType(),
            program.getTrainer().getId(),
            program.getTrainer().getName(),
            program.getCapacity(),
            program.getStartAt(),
            program.getEndAt(),
            program.getDescription(),
            program.getCreatedAt()
        );
    }
}
