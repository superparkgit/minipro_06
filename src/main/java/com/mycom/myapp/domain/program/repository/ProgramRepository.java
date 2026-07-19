package com.mycom.myapp.domain.program.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    // 타입별 프로그램 목록
    List<Program> findByType(ProgramType type);

    // 특정 날짜의 프로그램 목록
    List<Program> findByStartAtBetween(LocalDateTime start, LocalDateTime end);

    // 타입 + 날짜 필터
    List<Program> findByTypeAndStartAtBetween(ProgramType type, LocalDateTime start, LocalDateTime end);
}