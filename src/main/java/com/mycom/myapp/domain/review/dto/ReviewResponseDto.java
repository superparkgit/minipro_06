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
    private Long userId;
    private String userName;
    private Long trainerId;
    private String trainerName;
    private int rating;
    private String content;
    private ReviewStatus status;
    private ReviewReplyResponseDto reply;
    private String reportReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponseDto from(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .reservationId(review.getReservation().getId())
                .programId(review.getProgram().getId())
                .programTitle(review.getProgram().getTitle())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .trainerId(review.getTrainer().getId())
                .trainerName(review.getTrainer().getName())
                .rating(review.getRating())
                .content(review.getContent())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    // 관리자 심사 목록: 신고 사유를 함께 노출한다.
    public static ReviewResponseDto from(Review review, String reportReason) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .reservationId(review.getReservation().getId())
                .programId(review.getProgram().getId())
                .programTitle(review.getProgram().getTitle())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .trainerId(review.getTrainer().getId())
                .trainerName(review.getTrainer().getName())
                .rating(review.getRating())
                .content(review.getContent())
                .status(review.getStatus())
                .reportReason(reportReason)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    // 신고되어 심사 대기 중(HIDDEN)인 리뷰는 공개 목록에서 내용을 가려서 노출한다.
    private static final String HIDDEN_CONTENT_PLACEHOLDER = "트레이너의 권리 침해 신고로 가려진 리뷰입니다.";

    public static ReviewResponseDto from(Review review, ReviewReplyResponseDto replyDto) {
        boolean hidden = review.getStatus() == ReviewStatus.HIDDEN;
        return ReviewResponseDto.builder()
                .id(review.getId())
                .reservationId(review.getReservation().getId())
                .programId(review.getProgram().getId())
                .programTitle(review.getProgram().getTitle())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .trainerId(review.getTrainer().getId())
                .trainerName(review.getTrainer().getName())
                .rating(review.getRating())
                .content(hidden ? HIDDEN_CONTENT_PLACEHOLDER : review.getContent())
                .status(review.getStatus())
                .reply(replyDto)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
