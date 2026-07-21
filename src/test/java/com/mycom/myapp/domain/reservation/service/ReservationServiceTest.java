package com.mycom.myapp.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;
import com.mycom.myapp.domain.program.entity.ProgramTrainer;
import com.mycom.myapp.domain.program.entity.ProgramTrainer.AssignmentRole;
import com.mycom.myapp.domain.program.repository.ProgramRepository;
import com.mycom.myapp.domain.program.repository.ProgramTrainerRepository;
import com.mycom.myapp.domain.reservation.dto.ReservationRequest;
import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.domain.reservation.dto.AttendanceRequest;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.entity.Reservation.AttendanceStatus;
import com.mycom.myapp.domain.reservation.entity.Reservation.ReservationStatus;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;
import com.mycom.myapp.global.exception.CustomException;
import com.mycom.myapp.global.exception.ErrorCode;

/**
 * ReservationService 단위 테스트 (Mockito)
 * - Repository 는 전부 Mock 처리하고 비즈니스 로직만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private ProgramTrainerRepository programTrainerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    // ===== 테스트용 객체 생성 헬퍼 =====

    private User user(Long id, String name) {
        return User.builder().id(id).email(name + "@test.com").password("pw").name(name).build();
    }

    private Program program(Long id, User trainer, int capacity) {
        return Program.builder()
                .id(id).title("아침 PT").type(ProgramType.PT)
                .capacity(capacity)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();
    }

    private Reservation reservation(Long id, User user, Program program) {
        return Reservation.builder().id(id).user(user).program(program).build(); // 기본 상태 PENDING
    }

    // ===== 예약 신청 =====

    @Test
    @DisplayName("예약 신청 성공 - 신청 직후 상태는 PENDING")
    void createReservation_success() {
        User member = user(1L, "회원");
        User trainer = user(10L, "트레이너");
        Program program = program(100L, trainer, 5);

        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(reservationRepository.existsByUserIdAndProgramIdAndStatusIn(anyLong(), anyLong(), anyCollection()))
                .willReturn(false);
        given(reservationRepository.countByProgramIdAndStatus(100L, ReservationStatus.APPROVED))
                .willReturn(0L);
        given(userRepository.findById(1L)).willReturn(Optional.of(member));
        given(reservationRepository.save(any(Reservation.class)))
                .willAnswer(inv -> inv.getArgument(0));

        ReservationResponse response =
                reservationService.createReservation(new ReservationRequest(100L), 1L);

        assertThat(response.status()).isEqualTo(ReservationStatus.PENDING);
        assertThat(response.programId()).isEqualTo(100L);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 신청 실패 - 존재하지 않는 프로그램")
    void createReservation_programNotFound() {
        given(programRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.createReservation(new ReservationRequest(999L), 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLASS_NOT_FOUND);
    }

    @Test
    @DisplayName("예약 신청 실패 - 이미 신청한 프로그램 (중복 방지)")
    void createReservation_duplicate() {
        Program program = program(100L, user(10L, "트레이너"), 5);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(reservationRepository.existsByUserIdAndProgramIdAndStatusIn(anyLong(), anyLong(), anyCollection()))
                .willReturn(true);

        assertThatThrownBy(() ->
                reservationService.createReservation(new ReservationRequest(100L), 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESERVATION);
    }

    @Test
    @DisplayName("예약 신청 실패 - 정원 마감")
    void createReservation_capacityFull() {
        Program program = program(100L, user(10L, "트레이너"), 5);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(reservationRepository.existsByUserIdAndProgramIdAndStatusIn(anyLong(), anyLong(), anyCollection()))
                .willReturn(false);
        given(reservationRepository.countByProgramIdAndStatus(100L, ReservationStatus.APPROVED))
                .willReturn(5L); // 정원 5명 다 참

        assertThatThrownBy(() ->
                reservationService.createReservation(new ReservationRequest(100L), 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CAPACITY_EXCEEDED);
    }

    // ===== 예약 취소 =====

    @Test
    @DisplayName("예약 취소 성공 - 본인의 PENDING 예약")
    void cancelReservation_success() {
        User member = user(1L, "회원");
        Reservation reservation = reservation(500L, member, program(100L, user(10L, "트레이너"), 5));
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));

        reservationService.cancelReservation(500L, 1L);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("예약 취소 실패 - 본인 예약이 아님")
    void cancelReservation_notOwner() {
        Reservation reservation = reservation(500L, user(1L, "회원"), program(100L, user(10L, "트레이너"), 5));
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(500L, 2L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_ACCESS_DENIED);
    }

    @Test
    @DisplayName("예약 취소 실패 - 이미 취소된 예약은 다시 취소 불가")
    void cancelReservation_alreadyCanceled() {
        Reservation reservation = reservation(500L, user(1L, "회원"), program(100L, user(10L, "트레이너"), 5));
        reservation.cancel();
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(500L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_CANCELED);
    }

    // ===== 예약 승인 =====

    @Test
    @DisplayName("예약 승인 성공 - 트레이너 본인 프로그램의 PENDING 예약")
    void approveReservation_success() {
        User trainer = user(10L, "트레이너");
        Program program = program(100L, trainer, 5);
        Reservation reservation = reservation(500L, user(1L, "회원"), program);

        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(reservationRepository.countByProgramIdAndStatus(100L, ReservationStatus.APPROVED))
                .willReturn(0L);
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);

        ReservationResponse response = reservationService.approveReservation(500L, 10L);

        assertThat(response.status()).isEqualTo(ReservationStatus.APPROVED);
    }

    @Test
    @DisplayName("예약 승인 실패 - 다른 트레이너의 프로그램")
    void approveReservation_notMyProgram() {
        Program program = program(100L, user(10L, "트레이너"), 5);
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 99L)).willReturn(false);

        assertThatThrownBy(() -> reservationService.approveReservation(500L, 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_ACCESS_DENIED);
    }

    @Test
    @DisplayName("예약 승인 실패 - 이미 처리된(PENDING 아닌) 예약")
    void approveReservation_notPending() {
        Program program = program(100L, user(10L, "트레이너"), 5);
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        reservation.approve(); // 이미 승인됨
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);

        assertThatThrownBy(() -> reservationService.approveReservation(500L, 10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("예약 승인 실패 - 승인 시점에 정원 마감")
    void approveReservation_capacityFull() {
        Program program = program(100L, user(10L, "트레이너"), 2);
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);
        given(reservationRepository.countByProgramIdAndStatus(100L, ReservationStatus.APPROVED))
                .willReturn(2L); // 정원 2명 다 참

        assertThatThrownBy(() -> reservationService.approveReservation(500L, 10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CAPACITY_EXCEEDED);
    }

    // ===== 예약자 목록 조회 (권한) =====

    @Test
    @DisplayName("예약자 목록 조회 성공 - 담당 트레이너 본인")
    void getReservationsByProgram_success() {
        User trainer = user(10L, "트레이너");
        Program program = program(100L, trainer, 5);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);
        given(reservationRepository.findByProgramId(100L))
                .willReturn(List.of(reservation(500L, user(1L, "회원"), program)));

        assertThat(reservationService.getReservationsByProgram(100L, 10L)).hasSize(1);
    }

    @Test
    @DisplayName("예약자 목록 조회 실패 - 담당 트레이너가 아니면 개인정보 접근 차단")
    void getReservationsByProgram_notOwner() {
        Program program = program(100L, user(10L, "트레이너"), 5);
        given(programRepository.findById(100L)).willReturn(Optional.of(program));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 99L)).willReturn(false);

        assertThatThrownBy(() -> reservationService.getReservationsByProgram(100L, 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLASS_ACCESS_DENIED);
    }

    // ===== 출석 처리 =====

    @Test
    @DisplayName("출석 처리 성공 - 종료된 수업의 승인 예약을 담당 트레이너가 처리")
    void markAttendance_success() {
        Program program = program(100L, user(10L, "트레이너"), 1);
        program.complete();
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        reservation.approve();
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);

        ReservationResponse response = reservationService.markAttendance(
                500L, 10L, new AttendanceRequest(AttendanceStatus.ATTENDED));

        assertThat(response.attendanceStatus()).isEqualTo(AttendanceStatus.ATTENDED);
    }

    // ===== 예약 거절 =====

    @Test
    @DisplayName("예약 거절 성공 - 상태가 REJECTED 로 변경")
    void rejectReservation_success() {
        Program program = program(100L, user(10L, "트레이너"), 5);
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);

        ReservationResponse response = reservationService.rejectReservation(500L, 10L);

        assertThat(response.status()).isEqualTo(ReservationStatus.REJECTED);
    }

    // ===== 폐강/종료 수업의 예약 처리 차단 =====

    @Test
    @DisplayName("예약 승인 실패 - 폐강된 수업의 대기 예약은 승인 불가")
    void approveReservation_canceledProgram() {
        Program program = program(100L, user(10L, "트레이너"), 5);
        program.cancel();   // CANCELED
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);

        assertThatThrownBy(() -> reservationService.approveReservation(500L, 10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("출석 처리 실패 - 수업이 종료되지 않았으면 출석 처리 불가")
    void markAttendance_programNotCompleted() {
        Program program = program(100L, user(10L, "트레이너"), 5);   // OPEN 상태
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        reservation.approve();
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);

        assertThatThrownBy(() -> reservationService.markAttendance(
                500L, 10L, new AttendanceRequest(AttendanceStatus.ATTENDED)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

        assertThat(reservation.getAttendanceStatus()).isEqualTo(AttendanceStatus.NOT_CHECKED);
    }

    // ===== 리뷰 작성 자격 검증 =====

    @Test
    @DisplayName("리뷰 자격 검증 성공 - 출석 완료 시 MAIN 트레이너 ID 반환")
    void verifyReviewEligible_success() {
        User trainer = user(10L, "트레이너");
        Program program = program(100L, trainer, 5);
        program.complete();
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        reservation.approve();
        reservation.markAttendance(AttendanceStatus.ATTENDED);

        ProgramTrainer mainAssignment = ProgramTrainer.builder()
                .program(program).trainer(trainer)
                .assignmentRole(AssignmentRole.MAIN)
                .build();

        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.findByProgramIdAndAssignmentRole(100L, AssignmentRole.MAIN))
                .willReturn(Optional.of(mainAssignment));

        Long trainerId = reservationService.verifyReviewEligible(500L, 1L);

        assertThat(trainerId).isEqualTo(10L);
    }

    @Test
    @DisplayName("리뷰 자격 검증 실패 - 결석한 회원은 리뷰 작성 불가")
    void verifyReviewEligible_noShow() {
        Program program = program(100L, user(10L, "트레이너"), 5);
        program.complete();
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        reservation.approve();
        reservation.markAttendance(AttendanceStatus.NO_SHOW);

        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.verifyReviewEligible(500L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("리뷰 자격 검증 실패 - 본인 예약이 아니면 차단")
    void verifyReviewEligible_notOwner() {
        Program program = program(100L, user(10L, "트레이너"), 5);
        program.complete();
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        reservation.approve();
        reservation.markAttendance(AttendanceStatus.ATTENDED);

        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.verifyReviewEligible(500L, 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }
}
