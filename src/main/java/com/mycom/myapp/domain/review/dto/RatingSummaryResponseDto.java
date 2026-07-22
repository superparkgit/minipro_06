package com.mycom.myapp.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RatingSummaryResponseDto {
    private Long id;
    private BigDecimal averageRating;
    private int reviewCount;
}
