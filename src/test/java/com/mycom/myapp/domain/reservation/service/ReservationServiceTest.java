package com.mycom.myapp.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;
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
        User user = User.builder().email(name + "@test.com").password("pw").name(name).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
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
                .isInstanceOf(ResponseStatusException.class);
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
                .isInstanceOf(ResponseStatusException.class);
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
                .isInstanceOf(ResponseStatusException.class);
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
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("예약 취소 실패 - 이미 취소된 예약은 다시 취소 불가")
    void cancelReservation_alreadyCanceled() {
        Reservation reservation = reservation(500L, user(1L, "회원"), program(100L, user(10L, "트레이너"), 5));
        reservation.cancel();
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(500L, 1L))
                .isInstanceOf(ResponseStatusException.class);
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
                .isInstanceOf(ResponseStatusException.class);
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
                .isInstanceOf(ResponseStatusException.class);
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
                .isInstanceOf(ResponseStatusException.class);
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
                .isInstanceOf(ResponseStatusException.class);
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
    @Test
    @DisplayName("예약 신청 실패 - 폐강된 프로그램은 예약할 수 없음")
    void createReservation_canceledProgram() {
        Program program = program(100L, user(10L, "트레이너"), 5);
        program.cancel();
        given(programRepository.findById(100L)).willReturn(Optional.of(program));

        assertThatThrownBy(() ->
                reservationService.createReservation(new ReservationRequest(100L), 1L))
                .isInstanceOf(ResponseStatusException.class);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 승인 실패 - 폐강된 프로그램의 예약은 승인할 수 없음")
    void approveReservation_canceledProgram() {
        Program program = program(100L, user(10L, "트레이너"), 5);
        program.cancel();
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);

        assertThatThrownBy(() -> reservationService.approveReservation(500L, 10L))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("출석 처리 실패 - 수업이 완료되지 않으면 출석 처리할 수 없음")
    void markAttendance_programNotCompleted() {
        Program program = program(100L, user(10L, "트레이너"), 1);
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        reservation.approve();
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);

        assertThatThrownBy(() -> reservationService.markAttendance(
                500L, 10L, new AttendanceRequest(AttendanceStatus.ATTENDED)))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(reservation.getAttendanceStatus()).isEqualTo(AttendanceStatus.NOT_CHECKED);
    }

    @Test
    @DisplayName("출석 처리 실패 - 담당 트레이너가 아니면 출석 처리 불가")
    void markAttendance_notAssignedTrainer() {
        Program program = program(100L, user(10L, "트레이너"), 1);
        program.complete();
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        reservation.approve();
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 99L)).willReturn(false);

        assertThatThrownBy(() -> reservationService.markAttendance(
                500L, 99L, new AttendanceRequest(AttendanceStatus.ATTENDED)))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(reservation.getAttendanceStatus()).isEqualTo(AttendanceStatus.NOT_CHECKED);
    }

    @Test
    @DisplayName("출석 처리 실패 - 승인되지 않은 예약은 출석 처리할 수 없음")
    void markAttendance_reservationNotApproved() {
        Program program = program(100L, user(10L, "트레이너"), 1);
        program.complete();
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);

        assertThatThrownBy(() -> reservationService.markAttendance(
                500L, 10L, new AttendanceRequest(AttendanceStatus.ATTENDED)))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(reservation.getAttendanceStatus()).isEqualTo(AttendanceStatus.NOT_CHECKED);
    }

    @Test
    @DisplayName("출석 처리 실패 - NOT_CHECKED 상태로 처리할 수 없음")
    void markAttendance_notCheckedStatus() {
        Program program = program(100L, user(10L, "트레이너"), 1);
        program.complete();
        Reservation reservation = reservation(500L, user(1L, "회원"), program);
        reservation.approve();
        given(reservationRepository.findById(500L)).willReturn(Optional.of(reservation));
        given(programTrainerRepository.existsByProgramIdAndTrainerId(100L, 10L)).willReturn(true);

        assertThatThrownBy(() -> reservationService.markAttendance(
                500L, 10L, new AttendanceRequest(AttendanceStatus.NOT_CHECKED)))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(reservation.getAttendanceStatus()).isEqualTo(AttendanceStatus.NOT_CHECKED);
    }
}
