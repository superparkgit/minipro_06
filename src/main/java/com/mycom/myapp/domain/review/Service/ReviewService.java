package com.mycom.myapp.domain.review.Service;

import com.mycom.myapp.domain.global.exception.AccessDeniedException;
import com.mycom.myapp.domain.global.exception.BusinessRuleException;
import com.mycom.myapp.domain.global.exception.ResourceNotFoundException;
import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.ProgramTrainer;
import com.mycom.myapp.domain.program.repository.ProgramTrainerRepository;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.review.dto.*;
import com.mycom.myapp.domain.review.entity.*;
import com.mycom.myapp.domain.review.repository.ReviewReplyRepository;
import com.mycom.myapp.domain.review.repository.ReviewReportRepository;
import com.mycom.myapp.domain.review.repository.ReviewRepository;
import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final ReservationRepository reservationRepository;
    private final ProgramTrainerRepository programTrainerRepository;
    private final UserRepository userRepository;

    /**
     * 리뷰 작성
     * - 예약자 본인만 가능
     * - ATTENDED 상태인 예약만 가능
     * - 예약 1건당 리뷰 1건 제한
     */
    @Transactional
    public ReviewResponseDto createReview(Long reservationId, Long userId, ReviewRequestDto requestDto) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("예약", reservationId));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 예약에만 리뷰를 작성할 수 있습니다.");
        }

        if (reservation.getAttendanceStatus() != Reservation.AttendanceStatus.ATTENDED) {
            throw new BusinessRuleException("참여 완료된 예약만 리뷰를 작성할 수 있습니다.");
        }

        // USER_DELETED(작성자 직접 삭제)인 경우 재작성 허용, 그 외 상태는 중복으로 간주
        if (reviewRepository.existsByReservationIdAndStatusNot(reservationId, ReviewStatus.USER_DELETED)) {
            throw new BusinessRuleException("이미 리뷰를 작성한 예약입니다.");
        }

        Program program = reservation.getProgram();
        validateReviewPeriod(program);

        int rating = requestDto.getRating();
        if (rating < 1 || rating > 5) {
            throw new BusinessRuleException("평점은 1~5 사이여야 합니다.");
        }

        User user = reservation.getUser();
        User trainer = programTrainerRepository
                .findByProgramIdAndAssignmentRole(program.getId(), ProgramTrainer.AssignmentRole.MAIN)
                .orElseThrow(() -> new ResourceNotFoundException("담당 트레이너를 찾을 수 없습니다."))
                .getTrainer();

        Review review = Review.builder()
                .reservation(reservation)
                .user(user)
                .program(program)
                .trainer(trainer)
                .rating(rating)
                .content(requestDto.getContent())
                .build();

        return ReviewResponseDto.from(reviewRepository.save(review));
    }

    /**
     * 리뷰 수정 (작성자 본인만)
     */
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, Long userId, ReviewRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("리뷰 수정 권한이 없습니다.");
        }

        if (review.getStatus() != ReviewStatus.VISIBLE) {
            throw new BusinessRuleException("수정할 수 없는 상태의 리뷰입니다.");
        }

        validateReviewPeriod(review.getProgram());

        int rating = requestDto.getRating();
        if (rating < 1 || rating > 5) {
            throw new BusinessRuleException("평점은 1~5 사이여야 합니다.");
        }

        review.update(rating, requestDto.getContent());
        return ReviewResponseDto.from(review);
    }

    /**
     * 리뷰 삭제 (작성자 본인만)
     * - USER_DELETED 처리 → 재작성 가능
     * - ADMIN_DELETED(관리자 삭제)된 리뷰는 작성자도 조작 불가
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("리뷰 삭제 권한이 없습니다.");
        }

        if (review.getStatus() == ReviewStatus.USER_DELETED) {
            throw new BusinessRuleException("이미 삭제된 리뷰입니다.");
        }

        if (review.getStatus() == ReviewStatus.ADMIN_DELETED) {
            throw new BusinessRuleException("관리자에 의해 삭제된 리뷰는 수정하거나 삭제할 수 없습니다.");
        }

        review.markUserDeleted();
    }

    /**
     * 리뷰 작성/수정 기한 검증 (프로그램 종료일로부터 30일 이내)
     */
    private void validateReviewPeriod(Program program) {
        if (LocalDateTime.now().isAfter(program.getEndAt().plusDays(30))) {
            throw new BusinessRuleException("프로그램 종료일로부터 30일 이내에만 리뷰를 작성 및 수정할 수 있습니다.");
        }
    }

    /**
     * 프로그램별 리뷰 목록 (VISIBLE만, 공개)
     */
    public Page<ReviewResponseDto> getReviewsByProgram(Long programId, Pageable pageable) {
        return reviewRepository.findByProgramIdAndStatus(programId, ReviewStatus.VISIBLE, pageable)
                .map(review -> {
                    ReviewReplyResponseDto replyDto = reviewReplyRepository.findByReviewId(review.getId())
                            .map(ReviewReplyResponseDto::from)
                            .orElse(null);
                    return ReviewResponseDto.from(review, replyDto);
                });
    }

    /**
     * 트레이너 답변 등록 (담당 트레이너 본인만, 1건 제한)
     */
    @Transactional
    public ReviewReplyResponseDto createReply(Long reviewId, Long trainerId, ReviewReplyRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));

        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", trainerId));

        boolean isTrainer = trainer.getUserRoles().stream()
                .map(com.mycom.myapp.domain.user.entity.UserRole::getRoleName)
                .anyMatch(r -> r == Role.ROLE_TRAINER);
        if (!isTrainer) {
            throw new AccessDeniedException("트레이너만 답변을 등록할 수 있습니다.");
        }

        if (!review.getTrainer().getId().equals(trainerId)) {
            throw new AccessDeniedException("해당 프로그램의 담당 트레이너가 아닙니다.");
        }

        if (reviewReplyRepository.existsByReviewId(reviewId)) {
            throw new BusinessRuleException("이미 답변이 등록된 리뷰입니다.");
        }

        ReviewReply reply = ReviewReply.builder()
                .review(review)
                .trainer(trainer)
                .content(requestDto.getContent())
                .build();

        return ReviewReplyResponseDto.from(reviewReplyRepository.save(reply));
    }

    /**
     * 삭제 요청 (담당 트레이너 → 리뷰 즉시 숨김)
     */
    @Transactional
    public ReviewReportResponseDto reportReview(Long reviewId, Long trainerId, ReviewReportRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));

        User reporter = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", trainerId));

        boolean isTrainer = reporter.getUserRoles().stream()
                .map(com.mycom.myapp.domain.user.entity.UserRole::getRoleName)
                .anyMatch(r -> r == Role.ROLE_TRAINER);
        if (!isTrainer) {
            throw new AccessDeniedException("트레이너만 삭제 요청을 할 수 있습니다.");
        }

        if (!review.getTrainer().getId().equals(trainerId)) {
            throw new AccessDeniedException("해당 프로그램의 담당 트레이너가 아닙니다.");
        }

        if (review.getStatus() != ReviewStatus.VISIBLE) {
            throw new BusinessRuleException("이미 숨김 또는 삭제된 리뷰입니다.");
        }

        if (reviewReportRepository.existsByReviewIdAndStatus(reviewId, ReportStatus.PENDING)) {
            throw new BusinessRuleException("이미 심사 대기 중인 신고가 있습니다.");
        }

        ReviewReport report = ReviewReport.builder()
                .review(review)
                .reporter(reporter)
                .reason(requestDto.getReason())
                .build();

        review.hide(); // 즉시 숨김

        return ReviewReportResponseDto.from(reviewReportRepository.save(report));
    }

    /**
     * 관리자 심사 대기 목록 (HIDDEN)
     */
    public Page<ReviewResponseDto> getHiddenReviews(Pageable pageable) {
        return reviewRepository.findByStatus(ReviewStatus.HIDDEN, pageable)
                .map(ReviewResponseDto::from);
    }

    /**
     * 관리자 승인/반려
     * - 승인(APPROVED): HIDDEN → DELETED
     * - 반려(REJECTED): HIDDEN → VISIBLE 복원
     */
    @Transactional
    public ReviewResponseDto decideReport(Long reviewId, AdminReviewDecisionRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));

        if (review.getStatus() != ReviewStatus.HIDDEN) {
            throw new BusinessRuleException("숨김 상태의 리뷰만 심사할 수 있습니다.");
        }

        ReviewReport report = reviewReportRepository.findByReviewIdAndStatus(reviewId, ReportStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("심사 대기 중인 신고가 없습니다."));

        ReportStatus decision = requestDto.getDecision();

        if (decision == ReportStatus.APPROVED) {
            report.approve();
            review.markAdminDeleted(); // 신고 승인 → 관리자 삭제
        } else if (decision == ReportStatus.REJECTED) {
            report.reject();
            review.restore();
        } else {
            throw new BusinessRuleException("심사 결정은 APPROVED 또는 REJECTED만 가능합니다.");
        }

        return ReviewResponseDto.from(review);
    }

    /**
     * 트레이너 평점 집계 (실시간 AVG 쿼리)
     */
    public RatingSummaryResponseDto getTrainerRating(Long trainerId) {
        Object[] result = reviewRepository.getTrainerRatingSummary(trainerId);
        long count = (Long) result[0];
        BigDecimal avg = count > 0
                ? BigDecimal.valueOf((Double) result[1]).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return RatingSummaryResponseDto.builder()
                .id(trainerId)
                .averageRating(avg)
                .reviewCount((int) count)
                .build();
    }

    /**
     * 프로그램 평점 집계 (실시간 AVG 쿼리)
     */
    public RatingSummaryResponseDto getProgramRating(Long programId) {
        Object[] result = reviewRepository.getProgramRatingSummary(programId);
        long count = (Long) result[0];
        BigDecimal avg = count > 0
                ? BigDecimal.valueOf((Double) result[1]).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return RatingSummaryResponseDto.builder()
                .id(programId)
                .averageRating(avg)
                .reviewCount((int) count)
                .build();
    }
}
