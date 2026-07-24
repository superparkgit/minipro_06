package com.mycom.myapp.domain.reservation.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mycom.myapp.domain.program.dto.ProgramResponse;
import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramStatus;
import com.mycom.myapp.domain.program.entity.ProgramTrainer.AssignmentRole;
import com.mycom.myapp.domain.program.repository.ProgramRepository;
import com.mycom.myapp.domain.program.repository.ProgramTrainerRepository;
import com.mycom.myapp.domain.reservation.dto.AttendanceRequest;
import com.mycom.myapp.domain.reservation.dto.ProgramStatsResponse;
import com.mycom.myapp.domain.reservation.dto.ReservationRequest;
import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.domain.reservation.dto.TrainerStatsResponse;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.entity.Reservation.ReservationStatus;
import com.mycom.myapp.domain.reservation.entity.Reservation.AttendanceStatus;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ProgramRepository programRepository;
    private final ProgramTrainerRepository programTrainerRepository;
    private final UserRepository userRepository;

    // 예약 신청 (신청 직후에는 PENDING 상태, 트레이너 승인 필요)
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, Long userId) {
        Program program = programRepository.findById(request.programId())
                .orElseThrow(() -> notFound("프로그램을 찾을 수 없습니다."));

        if (program.getStatus() != ProgramStatus.OPEN) {
            throw badRequest("예약 신청이 가능한 프로그램이 아닙니다.");
        }

        // 중복 예약 방지 (대기 중이거나 이미 승인된 예약이 있으면 재신청 불가)
        if (reservationRepository.existsByUserIdAndProgramIdAndStatusIn(
                userId, program.getId(), List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED))) {
            throw conflict("이미 신청한 프로그램입니다.");
        }

        // 정원 체크 (승인된 인원 기준)
        long approvedCount = reservationRepository.countByProgramIdAndStatus(
                program.getId(), ReservationStatus.APPROVED);
        if (approvedCount >= program.getCapacity()) {
            throw conflict("정원이 마감된 프로그램입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> notFound("사용자를 찾을 수 없습니다."));

        Reservation reservation = Reservation.builder()
                .user(user)
                .program(program)
                .build();

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    // 내 예약 목록
    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    // 예약 취소 (본인 확인, 대기/승인 상태만 취소 가능)
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> notFound("예약을 찾을 수 없습니다."));

        if (!reservation.getUser().getId().equals(userId)) {
            throw forbidden("본인의 예약만 취소할 수 있습니다.");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.APPROVED) {
            throw badRequest("취소할 수 없는 예약 상태입니다.");
        }
        if (reservation.getAttendanceStatus() == AttendanceStatus.ATTENDED
                || reservation.getAttendanceStatus() == AttendanceStatus.NO_SHOW) {
            throw badRequest("출석 처리된 예약은 취소할 수 없습니다.");
        }
        reservation.cancel();
    }

    // 예약 승인 (TRAINER, 본인 프로그램만)
    @Transactional
    public ReservationResponse approveReservation(Long reservationId, Long trainerId) {
        Reservation reservation = getPendingReservationOfTrainer(reservationId, trainerId);

        // 정원 체크 (승인 시점에 다시 확인)
        long approvedCount = reservationRepository.countByProgramIdAndStatus(
                reservation.getProgram().getId(), ReservationStatus.APPROVED);
        if (approvedCount >= reservation.getProgram().getCapacity()) {
            throw conflict("정원이 마감되어 승인할 수 없습니다.");
        }

        reservation.approve();
        return ReservationResponse.from(reservation);
    }

    // 예약 거절 (TRAINER, 본인 프로그램만)
    @Transactional
    public ReservationResponse rejectReservation(Long reservationId, Long trainerId) {
        Reservation reservation = getPendingReservationOfTrainer(reservationId, trainerId);
        reservation.reject();
        return ReservationResponse.from(reservation);
    }

    // 공통 검증: 예약 존재 + 본인(트레이너) 프로그램 + PENDING 상태
    private Reservation getPendingReservationOfTrainer(Long reservationId, Long trainerId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> notFound("예약을 찾을 수 없습니다."));

        if (!programTrainerRepository.existsByProgramIdAndTrainerId(
                reservation.getProgram().getId(), trainerId)) {
            throw forbidden("담당 트레이너만 처리할 수 있습니다.");
        }
        // 폐강·종료된 수업의 대기 예약은 승인/거절 대상이 아니다
        ProgramStatus programStatus = reservation.getProgram().getStatus();
        if (programStatus == ProgramStatus.CANCELED || programStatus == ProgramStatus.COMPLETED) {
            throw badRequest("종료되었거나 폐강된 수업입니다.");
        }
        if (!reservation.isPending()) {
            throw badRequest("대기 중인 예약만 처리할 수 있습니다.");
        }
        return reservation;
    }

    // 특정 프로그램의 예약자 목록 (TRAINER, 본인 프로그램만)
    // 예약자 개인정보가 포함되므로 담당 트레이너 본인인지 반드시 확인
    public List<ReservationResponse> getReservationsByProgram(Long programId, Long trainerId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> notFound("프로그램을 찾을 수 없습니다."));

        if (!programTrainerRepository.existsByProgramIdAndTrainerId(programId, trainerId)) {
            throw forbidden("담당 트레이너만 예약자 목록을 조회할 수 있습니다.");
        }

        return reservationRepository.findByProgramId(programId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse markAttendance(
            Long reservationId, Long trainerId, AttendanceRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> notFound("예약을 찾을 수 없습니다."));

        if (!programTrainerRepository.existsByProgramIdAndTrainerId(
                reservation.getProgram().getId(), trainerId)) {
            throw forbidden("담당 트레이너만 출석 처리할 수 있습니다.");
        }
        if (reservation.getProgram().getStatus() != ProgramStatus.COMPLETED
                || reservation.getStatus() != ReservationStatus.APPROVED) {
            throw badRequest("종료된 수업의 승인 예약만 출석 처리할 수 있습니다.");
        }
        if (request.attendanceStatus() == AttendanceStatus.NOT_CHECKED) {
            throw badRequest("ATTENDED 또는 NO_SHOW만 처리할 수 있습니다.");
        }

        reservation.markAttendance(request.attendanceStatus());
        return ReservationResponse.from(reservation);
    }

    // ===== 리뷰 연동 (용재님 ReviewService에서 호출) =====

    /**
     * 리뷰 작성 자격을 검증하고, 리뷰 대상이 될 MAIN 트레이너 id를 반환한다.
     *
     * 검증 항목
     *  1. 예약 존재
     *  2. 예약자 본인
     *  3. 예약 상태 APPROVED
     *  4. 출석 상태 ATTENDED
     *  5. 수업 상태 COMPLETED
     *
     * @return 해당 프로그램의 MAIN 트레이너 id
     */
    public Long verifyReviewEligible(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> notFound("예약을 찾을 수 없습니다."));

        if (!reservation.getUser().getId().equals(userId)) {
            throw forbidden("본인의 예약으로만 리뷰를 작성할 수 있습니다.");
        }
        if (reservation.getStatus() != ReservationStatus.APPROVED) {
            throw badRequest("승인된 예약만 리뷰를 작성할 수 있습니다.");
        }
        if (reservation.getAttendanceStatus() != AttendanceStatus.ATTENDED) {
            throw badRequest("출석한 예약만 리뷰를 작성할 수 있습니다.");
        }
        if (reservation.getProgram().getStatus() != ProgramStatus.COMPLETED) {
            throw badRequest("수업 완료 후에만 리뷰를 작성할 수 있습니다.");
        }

        return programTrainerRepository
                .findByProgramIdAndAssignmentRole(reservation.getProgram().getId(), AssignmentRole.MAIN)
                .orElseThrow(() -> notFound("대표 트레이너를 찾을 수 없습니다."))
                .getTrainer().getId();
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    // ===== 통계 =====

    // 예약이 한 건도 없는 프로그램 목록 (NOT EXISTS 서브쿼리)
    public List<ProgramResponse> getProgramsWithoutReservation() {
        return reservationRepository.findProgramsWithoutReservation().stream()
                .map(ProgramResponse::from)
                .toList();
    }

    // 트레이너별 이번 달 승인된 예약 건수 (JOIN + GROUP BY)
    public List<TrainerStatsResponse> getMonthlyApprovedCountByTrainer() {
        LocalDateTime start = YearMonth.now().atDay(1).atStartOfDay();          // 이번 달 1일 00:00
        LocalDateTime end = start.plusMonths(1);                                // 다음 달 1일 00:00
        return reservationRepository.countApprovedByTrainer(
                ReservationStatus.APPROVED, AssignmentRole.MAIN, start, end);
    }

    // 승인 건수 기준 인기 프로그램 TOP N (JOIN + GROUP BY)
    public List<ProgramStatsResponse> getPopularPrograms(int limit) {
        return reservationRepository.findPopularPrograms(ReservationStatus.APPROVED).stream()
                .limit(limit)
                .toList();
    }
}
