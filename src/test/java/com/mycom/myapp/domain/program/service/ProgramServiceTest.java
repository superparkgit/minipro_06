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

import com.mycom.myapp.domain.program.dto.ProgramRequest;
import com.mycom.myapp.domain.program.dto.ProgramResponse;
import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;
import com.mycom.myapp.domain.program.repository.ProgramRepository;
import com.mycom.myapp.domain.program.repository.ProgramTrainerRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;
import com.mycom.myapp.global.exception.CustomException;
import com.mycom.myapp.global.exception.ErrorCode;

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
        return User.builder().id(id).email("trainer@test.com").password("pw").name("트레이너").build();
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
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLASS_ACCESS_DENIED);
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
                .isInstanceOf(CustomException.class);
        verify(programRepository, never()).delete(any(Program.class));
    }

    @Test
    @DisplayName("프로그램 상세 조회 실패 - 존재하지 않는 프로그램")
    void getProgram_notFound() {
        given(programRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> programService.getProgram(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLASS_NOT_FOUND);
    }

    // ===== 수업 완료 처리 =====

    @Test
    @DisplayName("수업 완료 성공 - MAIN 트레이너가 처리하면 COMPLETED 로 변경")
    void completeProgram_success() {
        Program program = program(100L);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(programTrainerRepository.existsByProgramIdAndTrainerIdAndAssignmentRole(any(), any(), any()))
                .willReturn(true);

        programService.completeProgram(100L, 10L);

        assertThat(program.getStatus()).isEqualTo(Program.ProgramStatus.COMPLETED);
    }

    @Test
    @DisplayName("수업 완료 실패 - 담당 MAIN 트레이너가 아니면 처리 불가")
    void completeProgram_notMainTrainer() {
        Program program = program(100L);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(programTrainerRepository.existsByProgramIdAndTrainerIdAndAssignmentRole(any(), any(), any()))
                .willReturn(false);

        assertThatThrownBy(() -> programService.completeProgram(100L, 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLASS_ACCESS_DENIED);

        assertThat(program.getStatus()).isEqualTo(Program.ProgramStatus.OPEN);
    }

    @Test
    @DisplayName("수업 완료 실패 - 이미 폐강된 수업은 완료 처리 불가")
    void completeProgram_canceled() {
        Program program = program(100L);
        program.cancel();   // CANCELED 상태로 변경
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(programTrainerRepository.existsByProgramIdAndTrainerIdAndAssignmentRole(any(), any(), any()))
                .willReturn(true);

        assertThatThrownBy(() -> programService.completeProgram(100L, 10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

        assertThat(program.getStatus()).isEqualTo(Program.ProgramStatus.CANCELED);
    }

    // ===== 보조 강사 배정 =====

    @Test
    @DisplayName("보조 강사 등록 실패 - 트레이너 역할이 없는 일반 회원은 배정 불가")
    void addAssistant_notTrainerRole() {
        Program program = program(100L);
        User normalMember = User.builder()
                .id(50L).email("user@test.com").password("pw").name("일반회원")
                .build();   // roles 비어 있음 = ROLE_TRAINER 없음

        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(programTrainerRepository.existsByProgramIdAndTrainerIdAndAssignmentRole(any(), any(), any()))
                .willReturn(true);
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 50L)).willReturn(false);
        given(userRepository.findById(50L)).willReturn(Optional.of(normalMember));

        assertThatThrownBy(() -> programService.addAssistant(100L, 50L, 10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }
}
