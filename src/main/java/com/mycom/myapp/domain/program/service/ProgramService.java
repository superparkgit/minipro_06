package com.mycom.myapp.domain.program.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mycom.myapp.domain.program.dto.ProgramRequest;
import com.mycom.myapp.domain.program.dto.ProgramResponse;
import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramStatus;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;
import com.mycom.myapp.domain.program.entity.ProgramTrainer;
import com.mycom.myapp.domain.program.entity.ProgramTrainer.AssignmentRole;
import com.mycom.myapp.domain.program.repository.ProgramRepository;
import com.mycom.myapp.domain.program.repository.ProgramTrainerRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.UserRole;
import com.mycom.myapp.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramService {

    private final ProgramRepository programRepository;
    private final ProgramTrainerRepository programTrainerRepository;
    private final UserRepository userRepository;

    // 프로그램 목록 조회 (타입/날짜 필터)
    public List<ProgramResponse> getPrograms(ProgramType type, LocalDate date) {
        List<Program> programs;

        if (type != null && date != null) {
            programs = programRepository.findByTypeAndStartAtBetween(
                    type, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        } else if (type != null) {
            programs = programRepository.findByType(type);
        } else if (date != null) {
            programs = programRepository.findByStartAtBetween(
                    date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        } else {
            programs = programRepository.findAll();
        }

        return programs.stream().map(ProgramResponse::from).toList();
    }

    // 프로그램 상세 조회
    public ProgramResponse getProgram(Long id) {
        return ProgramResponse.from(findProgramById(id));
    }

    // 프로그램 등록 (TRAINER)
    @Transactional
    public ProgramResponse createProgram(ProgramRequest request, Long trainerId) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> notFound("사용자를 찾을 수 없습니다."));

        Program program = Program.builder()
                .title(request.title())
                .type(request.type())
                .capacity(request.capacity())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .description(request.description())
                .build();

        Program savedProgram = programRepository.save(program);
        ProgramTrainer assignment = ProgramTrainer.builder()
                .program(savedProgram)
                .trainer(trainer)
                .assignmentRole(AssignmentRole.MAIN)
                .build();
        savedProgram.addTrainerAssignment(assignment);
        programTrainerRepository.save(assignment);

        return ProgramResponse.from(savedProgram);
    }

    // 프로그램 수정 (본인 확인)
    @Transactional
    public ProgramResponse updateProgram(Long id, ProgramRequest request, Long trainerId) {
        Program program = findProgramById(id);
        validateMainTrainer(program.getId(), trainerId);

        program.update(request.title(), request.type(), request.capacity(),
                request.startAt(), request.endAt(), request.description());
        return ProgramResponse.from(program);
    }

    // 프로그램 삭제 (본인 확인)
    @Transactional
    public void deleteProgram(Long id, Long trainerId) {
        Program program = findProgramById(id);
        validateMainTrainer(program.getId(), trainerId);
        program.cancel();
    }

    @Transactional
    public void cancelProgram(Long id, Long trainerId) {
        Program program = findProgramById(id);
        validateMainTrainer(program.getId(), trainerId);
        program.cancel();
    }

    // 수업 완료 처리 (MAIN 트레이너) - COMPLETED가 되어야 출석 처리가 가능하다
    @Transactional
    public void completeProgram(Long id, Long trainerId) {
        Program program = findProgramById(id);
        validateMainTrainer(program.getId(), trainerId);

        if (program.getStatus() == ProgramStatus.CANCELED) {
            throw badRequest("폐강된 수업은 완료 처리할 수 없습니다.");
        }
        program.complete();
    }

    @Transactional
    public ProgramResponse addAssistant(Long programId, Long targetTrainerId, Long requesterId) {
        Program program = findProgramById(programId);
        validateMainTrainer(programId, requesterId);

        if (programTrainerRepository.existsByProgramIdAndTrainerId(programId, targetTrainerId)) {
            throw badRequest("이미 담당 강사로 등록된 사용자입니다.");
        }

        User trainer = userRepository.findById(targetTrainerId)
                .orElseThrow(() -> notFound("사용자를 찾을 수 없습니다."));

        // 일반 회원(ROLE_USER)이 보조 강사로 등록되지 않도록 역할 확인
        boolean isTrainer = trainer.getUserRoles().stream()
                .map(UserRole::getRoleName)
                .anyMatch(role -> role == Role.ROLE_TRAINER);
        if (!isTrainer) {
            throw badRequest("트레이너 역할을 가진 사용자만 보조 강사로 등록할 수 있습니다.");
        }

        ProgramTrainer assignment = ProgramTrainer.builder()
                .program(program)
                .trainer(trainer)
                .assignmentRole(AssignmentRole.ASSISTANT)
                .build();
        program.addTrainerAssignment(assignment);
        programTrainerRepository.save(assignment);
        return ProgramResponse.from(program);
    }

    @Transactional
    public void removeAssistant(Long programId, Long targetTrainerId, Long requesterId) {
        validateMainTrainer(programId, requesterId);
        ProgramTrainer assignment = programTrainerRepository.findByProgramIdAndTrainerId(programId, targetTrainerId)
                .orElseThrow(() -> notFound("담당 강사를 찾을 수 없습니다."));
        if (assignment.getAssignmentRole() == AssignmentRole.MAIN) {
            throw badRequest("MAIN 트레이너는 제거할 수 없습니다.");
        }
        programTrainerRepository.delete(assignment);
    }

    private Program findProgramById(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> notFound("프로그램을 찾을 수 없습니다."));
    }

    private void validateMainTrainer(Long programId, Long trainerId) {
        if (!programTrainerRepository.existsByProgramIdAndTrainerIdAndAssignmentRole(
                programId, trainerId, AssignmentRole.MAIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "대표 트레이너만 처리할 수 있습니다.");
        }
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
