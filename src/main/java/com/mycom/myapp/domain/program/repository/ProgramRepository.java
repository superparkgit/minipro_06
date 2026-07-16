package com.mycom.myapp.domain.program.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.program.entity.Program;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    // TODO: 박서희 - 필터(type, date) 쿼리 메서드 또는 @Query 추가
}
