package com.mycom.myapp.domain.review.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.review.entity.ReviewReply;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewReplyResponseDto {
    private Long id;
    private Long reviewId;
    private String trainerName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewReplyResponseDto from(ReviewReply reply) {
        return ReviewReplyResponseDto.builder()
                .id(reply.getId())
                .reviewId(reply.getReview().getId())
                .trainerName(reply.getTrainer().getName())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }
}
