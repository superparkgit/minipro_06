package com.mycom.myapp.domain.program.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.program.dto.ProgramRequest;
import com.mycom.myapp.domain.program.dto.ProgramResponse;
import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;
import com.mycom.myapp.domain.program.repository.ProgramRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramService {

    private final ProgramRepository programRepository;
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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Program program = Program.builder()
                .title(request.title())
                .type(request.type())
                .trainer(trainer)
                .capacity(request.capacity())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .description(request.description())
                .build();

        return ProgramResponse.from(programRepository.save(program));
    }

    // 프로그램 수정 (본인 확인)
    @Transactional
    public ProgramResponse updateProgram(Long id, ProgramRequest request, Long trainerId) {
        Program program = findProgramById(id);
        validateOwner(program, trainerId);

        program.update(request.title(), request.type(), request.capacity(),
                request.startAt(), request.endAt(), request.description());
        return ProgramResponse.from(program);
    }

    // 프로그램 삭제 (본인 확인)
    @Transactional
    public void deleteProgram(Long id, Long trainerId) {
        Program program = findProgramById(id);
        validateOwner(program, trainerId);
        programRepository.delete(program);
    }

    private Program findProgramById(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로그램입니다."));
    }

    private void validateOwner(Program program, Long trainerId) {
        if (!program.getTrainer().getId().equals(trainerId)) {
            throw new IllegalStateException("본인이 담당한 프로그램만 수정/삭제할 수 있습니다.");
        }
    }
}