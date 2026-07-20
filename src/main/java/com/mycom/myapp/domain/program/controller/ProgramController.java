package com.mycom.myapp.domain.program.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.program.dto.ProgramRequest;
import com.mycom.myapp.domain.program.dto.ProgramResponse;
import com.mycom.myapp.domain.program.dto.AddTrainerRequest;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;
import com.mycom.myapp.domain.program.service.ProgramService;
import com.mycom.myapp.domain.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    // 프로그램 목록 (필터: 타입, 날짜)
    @GetMapping
    public ResponseEntity<List<ProgramResponse>> getPrograms(
            @RequestParam(required = false) ProgramType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(programService.getPrograms(type, date));
    }

    // 프로그램 상세
    @GetMapping("/{id}")
    public ResponseEntity<ProgramResponse> getProgram(@PathVariable Long id) {
        return ResponseEntity.ok(programService.getProgram(id));
    }

    // 프로그램 등록 (TRAINER)
    @PostMapping
    public ResponseEntity<ProgramResponse> createProgram(
            @Valid @RequestBody ProgramRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProgramResponse response = programService.createProgram(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 프로그램 수정 (TRAINER, 본인)
    @PatchMapping("/{id}")
    public ResponseEntity<ProgramResponse> updateProgram(
            @PathVariable Long id,
            @Valid @RequestBody ProgramRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(programService.updateProgram(id, request, userDetails.getUserId()));
    }

    // 프로그램 삭제 (TRAINER, 본인)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        programService.deleteProgram(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelProgram(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        programService.cancelProgram(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    // 수업 완료 처리 (MAIN) - COMPLETED가 되어야 출석 처리가 가능하다
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> completeProgram(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        programService.completeProgram(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/trainers")
    public ResponseEntity<ProgramResponse> addAssistant(
            @PathVariable Long id,
            @Valid @RequestBody AddTrainerRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(programService.addAssistant(id, request.trainerId(), userDetails.getUserId()));
    }

    @DeleteMapping("/{id}/trainers/{trainerId}")
    public ResponseEntity<Void> removeAssistant(
            @PathVariable Long id,
            @PathVariable Long trainerId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        programService.removeAssistant(id, trainerId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
