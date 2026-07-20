package com.mycom.myapp.domain.reservation.dto;

/**
 * 인기 프로그램 통계용 DTO.
 * 프로그램별 승인된 예약담.
 */
public record ProgramStatsResponse(
    Long programId,
    String programTitle,
    long approvedCount
) {}
