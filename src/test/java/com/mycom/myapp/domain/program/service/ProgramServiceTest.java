package com.mycom.myapp.domain.program.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.mycom.myapp.domain.program.dto.ProgramRequest;
import com.mycom.myapp.domain.program.dto.ProgramResponse;
import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;
import com.mycom.myapp.domain.program.entity.ProgramTrainer;
import com.mycom.myapp.domain.program.entity.ProgramTrainer.AssignmentRole;
import com.mycom.myapp.domain.program.repository.ProgramRepository;
import com.mycom.myapp.domain.program.repository.ProgramTrainerRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;

/**
 * ProgramService 단위 테스트 (Mockito)
 * - 트레이너 본인 확인(소유자 검증) 로직을 중심으로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private ProgramTrainerRepository programTrainerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProgramService programService;

    private User trainer(Long id) {
        User trainer = User.builder().email("trainer@test.com").password("pw").name("트레이너").build();
        ReflectionTestUtils.setField(trainer, "id", id);
        return trainer;
    }

    private Program program(Long id) {
        return Program.builder()
                .id(id).title("아침 PT").type(ProgramType.PT)
                .capacity(5)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();
    }

    private ProgramRequest request(String title) {
        return new ProgramRequest(title, ProgramType.GROUP, 10,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(1),
                "그룹 수업");
    }

    @Test
    @DisplayName("프로그램 등록 성공 - 트레이너가 등록")
    void createProgram_success() {
        given(userRepository.findById(10L)).willReturn(Optional.of(trainer(10L)));
        given(programRepository.save(any(Program.class))).willAnswer(inv -> inv.getArgument(0));

        ProgramResponse response = programService.createProgram(request("새 수업"), 10L);

        assertThat(response.title()).isEqualTo("새 수업");
        assertThat(response.trainers()).singleElement()
                .extracting(ProgramResponse.TrainerResponse::id)
                .isEqualTo(10L);
        verify(programRepository).save(any(Program.class));
    }

    @Test
    @DisplayName("프로그램 수정 성공 - 본인 프로그램")
    void updateProgram_success() {
        Program program = program(100L);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(programTrainerRepository.existsByProgramIdAndTrainerIdAndAssignmentRole(any(), any(), any()))
                .willReturn(true);

        ProgramResponse response = programService.updateProgram(100L, request("수정된 수업"), 10L);

        assertThat(response.title()).isEqualTo("수정된 수업");
        assertThat(response.type()).isEqualTo(ProgramType.GROUP);
    }

    @Test
    @DisplayName("프로그램 수정 실패 - 다른 트레이너의 프로그램")
    void updateProgram_notOwner() {
        Program program = program(100L);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));

        assertThatThrownBy(() -> programService.updateProgram(100L, request("수정"), 99L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("프로그램 삭제 요청 성공 - 본인 프로그램은 폐강 상태로 변경")
    void deleteProgram_success() {
        Program program = program(100L);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(programTrainerRepository.existsByProgramIdAndTrainerIdAndAssignmentRole(any(), any(), any()))
                .willReturn(true);

        programService.deleteProgram(100L, 10L);

        assertThat(program.getStatus()).isEqualTo(Program.ProgramStatus.CANCELED);
    }

    @Test
    @DisplayName("프로그램 삭제 실패 - 다른 트레이너의 프로그램이면 삭제 안 됨")
    void deleteProgram_notOwner() {
        Program program = program(100L);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));

        assertThatThrownBy(() -> programService.deleteProgram(100L, 99L))
                .isInstanceOf(ResponseStatusException.class);
        verify(programRepository, never()).delete(any(Program.class));
    }

    @Test
    @DisplayName("프로그램 상세 조회 실패 - 존재하지 않는 프로그램")
    void getProgram_notFound() {
        given(programRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> programService.getProgram(999L))
                .isInstanceOf(ResponseStatusException.class);
    }
    @Test
    @DisplayName("수업 완료 실패 - 폐강된 프로그램은 완료 처리할 수 없음")
    void completeProgram_canceledProgram() {
        Program program = program(100L);
        program.cancel();
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(programTrainerRepository.existsByProgramIdAndTrainerIdAndAssignmentRole(any(), any(), any()))
                .willReturn(true);

        assertThatThrownBy(() -> programService.completeProgram(100L, 10L))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(program.getStatus()).isEqualTo(Program.ProgramStatus.CANCELED);
    }

    @Test
    @DisplayName("수업 완료 실패 - MAIN이 아닌 트레이너는 완료 처리할 수 없음")
    void completeProgram_notMainTrainer() {
        Program program = program(100L);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(programTrainerRepository.existsByProgramIdAndTrainerIdAndAssignmentRole(
                100L, 20L, AssignmentRole.MAIN)).willReturn(false);

        assertThatThrownBy(() -> programService.completeProgram(100L, 20L))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(program.getStatus()).isEqualTo(Program.ProgramStatus.OPEN);
    }

    @Test
    @DisplayName("보조 강사 추가 실패 - 트레이너 역할이 아닌 회원은 추가할 수 없음")
    void addAssistant_nonTrainer() {
        Program program = program(100L);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(programTrainerRepository.existsByProgramIdAndTrainerIdAndAssignmentRole(any(), any(), any()))
                .willReturn(true);
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 20L)).willReturn(false);
        given(userRepository.findById(20L)).willReturn(Optional.of(trainer(20L)));

        assertThatThrownBy(() -> programService.addAssistant(100L, 20L, 10L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("보조 강사 제거 실패 - MAIN 트레이너는 제거할 수 없음")
    void removeAssistant_mainTrainer() {
        Program program = program(100L);
        User mainTrainer = trainer(10L);
        ProgramTrainer mainAssignment = ProgramTrainer.builder()
                .program(program)
                .trainer(mainTrainer)
                .assignmentRole(AssignmentRole.MAIN)
                .build();

        given(programTrainerRepository.existsByProgramIdAndTrainerIdAndAssignmentRole(any(), any(), any()))
                .willReturn(true);
        given(programTrainerRepository.findByProgramIdAndTrainerId(100L, 10L))
                .willReturn(Optional.of(mainAssignment));

        assertThatThrownBy(() -> programService.removeAssistant(100L, 10L, 10L))
                .isInstanceOf(ResponseStatusException.class);
        verify(programTrainerRepository, never()).delete(any(ProgramTrainer.class));
    }
}
