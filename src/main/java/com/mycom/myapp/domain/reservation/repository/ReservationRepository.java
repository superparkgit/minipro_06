package com.mycom.myapp.domain.reservation.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.reservation.dto.ProgramStatsResponse;
import com.mycom.myapp.domain.reservation.dto.TrainerStatsResponse;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.entity.Reservation.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 내 예약 목록
    List<Reservation> findByUserId(Long userId);

    // 특정 프로그램의 예약자 목록 (TRAINER용)
    List<Reservation> findByProgramId(Long programId);

    // 중복 예약 확인 (대기 중이거나 승인된 예약이 이미 있는지)
    boolean existsByUserIdAndProgramIdAndStatusIn(Long userId, Long programId, Collection<ReservationStatus> statuses);

    // 현재 예약 인원 수 (정원 체크용 - 승인된 예약 기준)
    long countByProgramIdAndStatus(Long programId, ReservationStatus status);

    // ===== 통계 쿼리 =====

    /**
     * [서브쿼리] 예약이 한 건도 없는 프로그램 목록.
     * NOT EXISTS 로 해당 프로그램을 참조하는 예약이 하나도 없는 프로그램만 조회.
     */
    @Query("""
            SELECT p FROM Program p
            WHERE NOT EXISTS (
                SELECT 1 FROM Reservation r
                WHERE r.program = p
            )
            """)
    List<Program> findProgramsWithoutReservation();

    /**
     * [JOIN + GROUP BY] 트레이너별 기간 내 승인된 예약 건수.
     * Reservation -> Program -> trainer(User) 를 조인해 트레이너별로 집계.
     */
    @Query("""
            SELECT new com.mycom.myapp.domain.reservation.dto.TrainerStatsResponse(
                t.id, t.name, COUNT(r.id))
            FROM Reservation r
            JOIN r.program p
            JOIN p.trainer t
            WHERE r.status = :status
              AND r.createdAt >= :start
              AND r.createdAt < :end
            GROUP BY t.id, t.name
            ORDER BY COUNT(r.id) DESC
            """)
    List<TrainerStatsResponse> countApprovedByTrainer(
            @Param("status") ReservationStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * [JOIN + GROUP BY] 승인 건수 기준 인기 프로그램.
     * 상위 N개는 Service 에서 Pageable/limit 로 잘라서 쓰.
     */
    @Query("""
            SELECT new com.mycom.myapp.domain.reservation.dto.ProgramStatsResponse(
                p.id, p.title, COUNT(r.id))
            FROM Reservation r
            JOIN r.program p
            WHERE r.status = :status
            GROUP BY p.id, p.title
            ORDER BY COUNT(r.id) DESC
            """)
    List<ProgramStatsResponse> findPopularPrograms(@Param("status") ReservationStatus status);
}