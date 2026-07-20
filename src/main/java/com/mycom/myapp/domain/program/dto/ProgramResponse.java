package com.mycom.myapp.domain.program.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;
import com.mycom.myapp.domain.program.entity.Program.ProgramStatus;
import com.mycom.myapp.domain.program.entity.ProgramTrainer;

public record ProgramResponse(
    Long id,
    String title,
    ProgramType type,
    List<TrainerResponse> trainers,
    int capacity,
    LocalDateTime startAt,
    LocalDateTime endAt,
    String description,
    ProgramStatus status,
    LocalDateTime createdAt
) {
    public static ProgramResponse from(Program program) {
        return new ProgramResponse(
            program.getId(),
            program.getTitle(),
            program.getType(),
            program.getTrainerAssignments().stream()
                .map(TrainerResponse::from)
                .toList(),
            program.getCapacity(),
            program.getStartAt(),
            program.getEndAt(),
            program.getDescription(),
            program.getStatus(),
            program.getCreatedAt()
        );
    }

    public record TrainerResponse(Long id, String name, ProgramTrainer.AssignmentRole assignmentRole) {
        private static TrainerResponse from(ProgramTrainer assignment) {
            return new TrainerResponse(
                assignment.getTrainer().getId(),
                assignment.getTrainer().getName(),
                assignment.getAssignmentRole()
            );
        }
    }
}
