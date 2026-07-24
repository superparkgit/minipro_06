package com.mycom.myapp.domain.program.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    // 목록 전체 조회 시 담당 트레이너를 fetch join으로 함께 가져온다 (N+1 방지).
    // trainerAssignments는 OneToMany라 트레이너가 여럿이면 행이 중복될 수 있어 DISTINCT로 제거한다.
    @Override
    @Query("SELECT DISTINCT p FROM Program p " +
            "LEFT JOIN FETCH p.trainerAssignments ta " +
            "LEFT JOIN FETCH ta.trainer")
    List<Program> findAll();

    // 타입별 프로그램 목록
    @Query("SELECT DISTINCT p FROM Program p " +
            "LEFT JOIN FETCH p.trainerAssignments ta " +
            "LEFT JOIN FETCH ta.trainer " +
            "WHERE p.type = :type")
    List<Program> findByType(@Param("type") ProgramType type);

    // 특정 날짜의 프로그램 목록
    @Query("SELECT DISTINCT p FROM Program p " +
            "LEFT JOIN FETCH p.trainerAssignments ta " +
            "LEFT JOIN FETCH ta.trainer " +
            "WHERE p.startAt BETWEEN :start AND :end")
    List<Program> findByStartAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 타입 + 날짜 필터
    @Query("SELECT DISTINCT p FROM Program p " +
            "LEFT JOIN FETCH p.trainerAssignments ta " +
            "LEFT JOIN FETCH ta.trainer " +
            "WHERE p.type = :type AND p.startAt BETWEEN :start AND :end")
    List<Program> findByTypeAndStartAtBetween(@Param("type") ProgramType type,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}