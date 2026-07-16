package com.mycom.myapp.domain.program.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.program.service.ProgramService;

import lombok.RequiredArgsConstructor;

/**
 * ProgramController - 박서희 담당
 * <p>
 * TODO: API 구현
 * - GET    /api/programs          : 프로그램 목록 (필터: 타입, 날짜)
 * - GET    /api/programs/{id}     : 프로그램 상세
 * - POST   /api/programs          : 프로그램 등록 (TRAINER)
 * - PATCH  /api/programs/{id}     : 프로그램 수정 (TRAINER, 본인)
 * - DELETE /api/programs/{id}     : 프로그램 삭제 (TRAINER, 본인)
 */
@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    // TODO: 박서희 - 컨트롤러 메서드 구현
}
