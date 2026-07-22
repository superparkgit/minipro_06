package com.mycom.myapp.domain.review.dto;

import com.mycom.myapp.domain.review.entity.ReportStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminReviewDecisionRequestDto {
    private ReportStatus decision;
}
