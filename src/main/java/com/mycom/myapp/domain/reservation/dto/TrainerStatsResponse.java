package com.mycom.myapp.domain.reservation.dto;

/**
 * 트레이너별 예약 통계용 DTO.
 * 트레이너별로 이번 달 승인된 예약 건수를 담는다.
 */
public record TrainerStatsResponse(
    Long trainerId,
    String trainerName,
    long approvedCount
) {}
