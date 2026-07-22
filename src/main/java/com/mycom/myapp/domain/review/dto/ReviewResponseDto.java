package com.mycom.myapp.domain.review.dto;

import com.mycom.myapp.domain.review.entity.Review;
import com.mycom.myapp.domain.review.entity.ReviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponseDto {
    private Long id;
    private Long reservationId;
    private Long programId;
    private String programTitle;
    private String userName;
    private String trainerName;
    private int rating;
    private String content;
    private ReviewStatus status;
    private ReviewReplyResponseDto reply;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponseDto from(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .reservationId(review.getReservation().getId())
                .programId(review.getProgram().getId())
                .programTitle(review.getProgram().getTitle())
                .userName(review.getUser().getName())
                .trainerName(review.getTrainer().getName())
                .rating(review.getRating())
                .content(review.getContent())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    public static ReviewResponseDto from(Review review, ReviewReplyResponseDto replyDto) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .reservationId(review.getReservation().getId())
                .programId(review.getProgram().getId())
                .programTitle(review.getProgram().getTitle())
                .userName(review.getUser().getName())
                .trainerName(review.getTrainer().getName())
                .rating(review.getRating())
                .content(review.getContent())
                .status(review.getStatus())
                .reply(replyDto)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
