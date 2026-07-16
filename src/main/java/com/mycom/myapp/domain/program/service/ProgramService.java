package com.mycom.myapp.domain.program.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.program.repository.ProgramRepository;

import lombok.RequiredArgsConstructor;

/**
 * ProgramService - 박서희 담당
 * <p>
 * TODO: 다음 API 구현
 * - getPrograms(type, date): 프로그램 목록 조회 (필터)
 * - getProgram(id): 프로그램 상세 조회
 * - createProgram(request, trainerId): 프로그램 등록 (TRAINER)
 * - updateProgram(id, request, trainerId): 프로그램 수정 (본인 확인)
 * - deleteProgram(id, trainerId): 프로그램 삭제 (본인 확인)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramService {

    private final ProgramRepository programRepository;

    // TODO: 박서희 - 서비스 메서드 구현
}
