package com.mycom.myapp.domain.review.dto;

import com.mycom.myapp.domain.review.entity.ReportStatus;
import com.mycom.myapp.domain.review.entity.ReviewReport;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewReportResponseDto {
    private Long id;
    private Long reviewId;
    private String reporterName;
    private String reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static ReviewReportResponseDto from(ReviewReport report) {
        return ReviewReportResponseDto.builder()
                .id(report.getId())
                .reviewId(report.getReview().getId())
                .reporterName(report.getReporter().getName())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}
