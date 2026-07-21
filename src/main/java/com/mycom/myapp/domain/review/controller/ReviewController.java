package com.mycom.myapp.domain.review.controller;

import com.mycom.myapp.domain.review.Service.ReviewService;
import com.mycom.myapp.domain.review.dto.*;
import com.mycom.myapp.domain.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 작성 (USER, 예약자 본인, ATTENDED 상태만)
     */
    @PostMapping("/reservations/{reservationId}/reviews")
    public ResponseEntity<ReviewResponseDto> createReview(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReviewRequestDto requestDto) {
        ReviewResponseDto response = reviewService.createReview(
                reservationId, userDetails.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 리뷰 수정 (작성자 본인)
     */
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReviewRequestDto requestDto) {
        ReviewResponseDto response = reviewService.updateReview(
                reviewId, userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 삭제 (작성자 본인)
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 프로그램별 리뷰 목록 (VISIBLE만, 전체 공개)
     */
    @GetMapping("/programs/{programId}/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByProgram(
            @PathVariable Long programId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponseDto> response = reviewService.getReviewsByProgram(programId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 트레이너 답변 등록 (담당 트레이너 본인)
     */
    @PostMapping("/reviews/{reviewId}/replies")
    public ResponseEntity<ReviewReplyResponseDto> createReply(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReviewReplyRequestDto requestDto) {
        ReviewReplyResponseDto response = reviewService.createReply(
                reviewId, userDetails.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 삭제 요청 / 신고 (담당 트레이너 본인)
     */
    @PostMapping("/reviews/{reviewId}/reports")
    public ResponseEntity<ReviewReportResponseDto> reportReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReviewReportRequestDto requestDto) {
        ReviewReportResponseDto response = reviewService.reportReview(
                reviewId, userDetails.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 관리자 심사 대기 목록 (HIDDEN 상태 리뷰)
     */
    @GetMapping("/admin/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getHiddenReviews(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponseDto> response = reviewService.getHiddenReviews(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 관리자 승인/반려 처리
     */
    @PatchMapping("/admin/reviews/{reviewId}/decision")
    public ResponseEntity<ReviewResponseDto> decideReport(
            @PathVariable Long reviewId,
            @RequestBody AdminReviewDecisionRequestDto requestDto) {
        ReviewResponseDto response = reviewService.decideReport(reviewId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 트레이너 평점 집계 조회 (전체 공개)
     */
    @GetMapping("/trainers/{trainerId}/rating")
    public ResponseEntity<RatingSummaryResponseDto> getTrainerRating(
            @PathVariable Long trainerId) {
        return ResponseEntity.ok(reviewService.getTrainerRating(trainerId));
    }

    /**
     * 프로그램 평점 집계 조회 (전체 공개)
     */
    @GetMapping("/programs/{programId}/rating")
    public ResponseEntity<RatingSummaryResponseDto> getProgramRating(
            @PathVariable Long programId) {
        return ResponseEntity.ok(reviewService.getProgramRating(programId));
    }
}
