package com.mycom.myapp.domain.review.Service;

import com.mycom.myapp.domain.global.exception.AccessDeniedException;
import com.mycom.myapp.domain.global.exception.BusinessRuleException;
import com.mycom.myapp.domain.global.exception.ResourceNotFoundException;
import com.mycom.myapp.domain.program.entity.Program;
import com.mycom.myapp.domain.program.entity.Program.ProgramType;
import com.mycom.myapp.domain.program.entity.ProgramTrainer;
import com.mycom.myapp.domain.program.repository.ProgramTrainerRepository;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.entity.Reservation.AttendanceStatus;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.review.dto.AdminReviewDecisionRequestDto;
import com.mycom.myapp.domain.review.dto.RatingSummaryResponseDto;
import com.mycom.myapp.domain.review.dto.ReviewReplyRequestDto;
import com.mycom.myapp.domain.review.dto.ReviewReplyResponseDto;
import com.mycom.myapp.domain.review.dto.ReviewReportRequestDto;
import com.mycom.myapp.domain.review.dto.ReviewReportResponseDto;
import com.mycom.myapp.domain.review.dto.ReviewRequestDto;
import com.mycom.myapp.domain.review.dto.ReviewResponseDto;
import com.mycom.myapp.domain.review.entity.ReportStatus;
import com.mycom.myapp.domain.review.entity.Review;
import com.mycom.myapp.domain.review.entity.ReviewReply;
import com.mycom.myapp.domain.review.entity.ReviewReport;
import com.mycom.myapp.domain.review.entity.ReviewStatus;
import com.mycom.myapp.domain.review.repository.ReviewReplyRepository;
import com.mycom.myapp.domain.review.repository.ReviewReportRepository;
import com.mycom.myapp.domain.review.repository.ReviewRepository;
import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.entity.UserRole;
import com.mycom.myapp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewReplyRepository reviewReplyRepository;
    @Mock private ReviewReportRepository reviewReportRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private ProgramTrainerRepository programTrainerRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private User trainer;
    private Program program;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        user = User.builder().email("user@test.com").password("pw").name("회원").build();
        ReflectionTestUtils.setField(user, "id", 1L);

        trainer = User.builder().email("trainer@test.com").password("pw").name("트레이너").build();
        ReflectionTestUtils.setField(trainer, "id", 10L);

        program = Program.builder()
                .title("테스트 프로그램").type(ProgramType.PT).capacity(5)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();
        ReflectionTestUtils.setField(program, "id", 100L);

        reservation = Reservation.builder()
                .user(user).program(program)
                .build();
        ReflectionTestUtils.setField(reservation, "id", 200L);
        // 출석 완료 상태로 설정
        reservation.approve();
        reservation.markAttendance(AttendanceStatus.ATTENDED);
    }

    private ReviewRequestDto reviewRequest(int rating, String content) {
        ReviewRequestDto dto = new ReviewRequestDto();
        ReflectionTestUtils.setField(dto, "rating", rating);
        ReflectionTestUtils.setField(dto, "content", content);
        return dto;
    }

    private Review buildReview(Long id) {
        Review review = Review.builder()
                .reservation(reservation)
                .user(user)
                .program(program)
                .trainer(trainer)
                .rating(5)
                .content("테스트 리뷰")
                .build();
        ReflectionTestUtils.setField(review, "id", id);
        return review;
    }

    private User trainerUserWithRole(Long id, String name) {
        UserRole trainerRole;
        try {
            var constructor = UserRole.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            trainerRole = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(trainerRole, "roleName", Role.ROLE_TRAINER);

        User trainerWithRole = User.builder()
                .email(name + "@test.com").password("pw").name(name)
                .userRoles(Set.of(trainerRole))
                .build();
        ReflectionTestUtils.setField(trainerWithRole, "id", id);
        return trainerWithRole;
    }

    private ReviewReplyRequestDto replyRequest(String content) {
        ReviewReplyRequestDto dto = new ReviewReplyRequestDto();
        ReflectionTestUtils.setField(dto, "content", content);
        return dto;
    }

    private ReviewReportRequestDto reportRequest(String reason) {
        ReviewReportRequestDto dto = new ReviewReportRequestDto();
        ReflectionTestUtils.setField(dto, "reason", reason);
        return dto;
    }

    private AdminReviewDecisionRequestDto decisionRequest(ReportStatus decision) {
        AdminReviewDecisionRequestDto dto = new AdminReviewDecisionRequestDto();
        ReflectionTestUtils.setField(dto, "decision", decision);
        return dto;
    }

    private ReviewReply buildReply(Long id, Review review, User trainerUser, String content) {
        ReviewReply reply = ReviewReply.builder().review(review).trainer(trainerUser).content(content).build();
        ReflectionTestUtils.setField(reply, "id", id);
        return reply;
    }

    private ReviewReport buildReport(Long id, Review review, User reporter, String reason) {
        ReviewReport report = ReviewReport.builder().review(review).reporter(reporter).reason(reason).build();
        ReflectionTestUtils.setField(report, "id", id);
        return report;
    }

    // ================================================================
    // 리뷰 작성 테스트
    // ================================================================
    @Nested
    @DisplayName("리뷰 작성 (createReview)")
    class CreateReview {

        @Test
        @DisplayName("성공 - 출석 완료된 예약에 리뷰 작성")
        void success() {
            ProgramTrainer pt = ProgramTrainer.builder()
                    .program(program).trainer(trainer)
                    .assignmentRole(ProgramTrainer.AssignmentRole.MAIN)
                    .build();

            given(reservationRepository.findById(200L)).willReturn(Optional.of(reservation));
            given(reviewRepository.existsByReservationIdAndStatusNot(200L, ReviewStatus.USER_DELETED)).willReturn(false);
            given(programTrainerRepository.findByProgramIdAndAssignmentRole(100L, ProgramTrainer.AssignmentRole.MAIN))
                    .willReturn(Optional.of(pt));
            given(reviewRepository.save(any(Review.class))).willAnswer(inv -> {
                Review saved = inv.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 1L);
                return saved;
            });

            ReviewResponseDto response = reviewService.createReview(200L, 1L, reviewRequest(5, "최고!"));

            assertThat(response.getRating()).isEqualTo(5);
            assertThat(response.getContent()).isEqualTo("최고!");
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("실패 - 본인 예약이 아닌 경우")
        void fail_notOwner() {
            given(reservationRepository.findById(200L)).willReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reviewService.createReview(200L, 999L, reviewRequest(5, "좋아요")))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - 출석 완료되지 않은 예약")
        void fail_notAttended() {
            Reservation pendingReservation = Reservation.builder()
                    .user(user).program(program).build();
            ReflectionTestUtils.setField(pendingReservation, "id", 300L);

            given(reservationRepository.findById(300L)).willReturn(Optional.of(pendingReservation));

            assertThatThrownBy(() -> reviewService.createReview(300L, 1L, reviewRequest(5, "좋아요")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("참여 완료");
        }

        @Test
        @DisplayName("실패 - 이미 리뷰를 작성한 예약 (중복)")
        void fail_duplicate() {
            given(reservationRepository.findById(200L)).willReturn(Optional.of(reservation));
            given(reviewRepository.existsByReservationIdAndStatusNot(200L, ReviewStatus.USER_DELETED)).willReturn(true);

            assertThatThrownBy(() -> reviewService.createReview(200L, 1L, reviewRequest(5, "좋아요")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("이미 리뷰");
        }

        @Test
        @DisplayName("실패 - 평점 범위 초과 (1~5)")
        void fail_invalidRating() {
            given(reservationRepository.findById(200L)).willReturn(Optional.of(reservation));
            given(reviewRepository.existsByReservationIdAndStatusNot(200L, ReviewStatus.USER_DELETED)).willReturn(false);

            assertThatThrownBy(() -> reviewService.createReview(200L, 1L, reviewRequest(6, "좋아요")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("평점");
        }

        @Test
        @DisplayName("성공 - USER_DELETED 상태의 리뷰가 있어도 재작성 가능")
        void success_rewriteAfterUserDeleted() {
            ProgramTrainer pt = ProgramTrainer.builder()
                    .program(program).trainer(trainer)
                    .assignmentRole(ProgramTrainer.AssignmentRole.MAIN)
                    .build();

            given(reservationRepository.findById(200L)).willReturn(Optional.of(reservation));
            // USER_DELETED 제외 시 존재하지 않음 → 재작성 허용
            given(reviewRepository.existsByReservationIdAndStatusNot(200L, ReviewStatus.USER_DELETED)).willReturn(false);
            given(programTrainerRepository.findByProgramIdAndAssignmentRole(100L, ProgramTrainer.AssignmentRole.MAIN))
                    .willReturn(Optional.of(pt));
            given(reviewRepository.save(any(Review.class))).willAnswer(inv -> {
                Review saved = inv.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 2L);
                return saved;
            });

            ReviewResponseDto response = reviewService.createReview(200L, 1L, reviewRequest(4, "재작성 리뷰"));

            assertThat(response.getRating()).isEqualTo(4);
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("실패 - 프로그램 종료일로부터 30일 경과")
        void fail_after30Days() {
            Program oldProgram = Program.builder()
                    .title("옛날 수업")
                    .endAt(LocalDateTime.now().minusDays(31))
                    .build();
            ReflectionTestUtils.setField(oldProgram, "id", 101L);
            
            Reservation oldReservation = Reservation.builder()
                    .user(user).program(oldProgram).build();
            ReflectionTestUtils.setField(oldReservation, "id", 301L);
            oldReservation.approve();
            oldReservation.markAttendance(Reservation.AttendanceStatus.ATTENDED);
            
            given(reservationRepository.findById(301L)).willReturn(Optional.of(oldReservation));
            given(reviewRepository.existsByReservationIdAndStatusNot(301L, ReviewStatus.USER_DELETED)).willReturn(false);
            
            assertThatThrownBy(() -> reviewService.createReview(301L, 1L, reviewRequest(5, "좋아요")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("30일 이내에만");
        }
    }

    // ================================================================
    // 리뷰 수정 테스트
    // ================================================================
    @Nested
    @DisplayName("리뷰 수정 (updateReview)")
    class UpdateReview {

        @Test
        @DisplayName("성공 - 작성자 본인이 수정")
        void success() {
            Review review = buildReview(1L);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            ReviewResponseDto response = reviewService.updateReview(1L, 1L, reviewRequest(3, "수정된 내용"));

            assertThat(response.getRating()).isEqualTo(3);
            assertThat(response.getContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("실패 - 다른 사용자가 수정 시도")
        void fail_notOwner() {
            Review review = buildReview(1L);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            assertThatThrownBy(() -> reviewService.updateReview(1L, 999L, reviewRequest(3, "수정")))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - VISIBLE이 아닌 상태의 리뷰는 수정 불가")
        void fail_notVisible() {
            Review review = buildReview(1L);
            review.hide(); // HIDDEN 상태
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            assertThatThrownBy(() -> reviewService.updateReview(1L, 1L, reviewRequest(3, "수정")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("수정할 수 없는");
        }

        @Test
        @DisplayName("실패 - 프로그램 종료일로부터 30일 경과 후 수정 시도")
        void fail_after30Days() {
            Program oldProgram = Program.builder()
                    .title("옛날 수업")
                    .endAt(LocalDateTime.now().minusDays(31))
                    .build();
            ReflectionTestUtils.setField(oldProgram, "id", 101L);
            
            Reservation oldReservation = Reservation.builder()
                    .user(user).program(oldProgram).build();
            ReflectionTestUtils.setField(oldReservation, "id", 301L);
            oldReservation.approve();
            oldReservation.markAttendance(Reservation.AttendanceStatus.ATTENDED);
            
            Review oldReview = Review.builder()
                .reservation(oldReservation)
                .user(user)
                .program(oldProgram)
                .trainer(trainer)
                .rating(5)
                .content("좋은 수업이었습니다")
                .build();
            ReflectionTestUtils.setField(oldReview, "id", 2L);

            given(reviewRepository.findById(2L)).willReturn(Optional.of(oldReview));

            assertThatThrownBy(() -> reviewService.updateReview(2L, 1L, reviewRequest(3, "수정 시도")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("30일 이내에만");
        }
    }

    // ================================================================
    // 리뷰 삭제 테스트
    // ================================================================
    @Nested
    @DisplayName("리뷰 삭제 (deleteReview)")
    class DeleteReview {

        @Test
        @DisplayName("성공 - 작성자 본인이 삭제 → USER_DELETED")
        void success() {
            Review review = buildReview(1L);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            reviewService.deleteReview(1L, 1L);

            assertThat(review.getStatus()).isEqualTo(ReviewStatus.USER_DELETED);
        }

        @Test
        @DisplayName("실패 - 다른 사용자가 삭제 시도")
        void fail_notOwner() {
            Review review = buildReview(1L);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            assertThatThrownBy(() -> reviewService.deleteReview(1L, 999L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - 이미 USER_DELETED인 리뷰 재삭제")
        void fail_alreadyUserDeleted() {
            Review review = buildReview(1L);
            review.markUserDeleted();
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            assertThatThrownBy(() -> reviewService.deleteReview(1L, 1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("이미 삭제");
        }

        @Test
        @DisplayName("실패 - ADMIN_DELETED 상태의 리뷰는 작성자도 삭제 불가")
        void fail_adminDeleted() {
            Review review = buildReview(1L);
            review.markAdminDeleted();
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            assertThatThrownBy(() -> reviewService.deleteReview(1L, 1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("관리자에 의해");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 리뷰")
        void fail_notFound() {
            given(reviewRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.deleteReview(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ================================================================
    // 프로그램별 리뷰 목록 조회 테스트
    // ================================================================
    @Nested
    @DisplayName("프로그램별 리뷰 목록 조회 (getReviewsByProgram)")
    class GetReviewsByProgram {

        @Test
        @DisplayName("성공 - 답변이 달린 리뷰는 답변 정보를 함께 반환")
        void success_withReply() {
            Review review = buildReview(1L);
            Pageable pageable = PageRequest.of(0, 10);
            given(reviewRepository.findByProgramIdAndStatus(100L, ReviewStatus.VISIBLE, pageable))
                    .willReturn(new PageImpl<>(List.of(review)));

            ReviewReply reply = buildReply(5L, review, trainer, "답변입니다");
            given(reviewReplyRepository.findByReviewIdIn(List.of(1L))).willReturn(List.of(reply));

            var result = reviewService.getReviewsByProgram(100L, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getReply()).isNotNull();
            assertThat(result.getContent().get(0).getReply().getContent()).isEqualTo("답변입니다");
        }

        @Test
        @DisplayName("성공 - 답변이 없는 리뷰는 reply가 null")
        void success_withoutReply() {
            Review review = buildReview(1L);
            Pageable pageable = PageRequest.of(0, 10);
            given(reviewRepository.findByProgramIdAndStatus(100L, ReviewStatus.VISIBLE, pageable))
                    .willReturn(new PageImpl<>(List.of(review)));
            given(reviewReplyRepository.findByReviewIdIn(List.of(1L))).willReturn(List.of());

            var result = reviewService.getReviewsByProgram(100L, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getReply()).isNull();
        }
    }

    // ================================================================
    // 트레이너 답변 등록 테스트
    // ================================================================
    @Nested
    @DisplayName("트레이너 답변 등록 (createReply)")
    class CreateReply {

        @Test
        @DisplayName("성공 - 담당 트레이너가 답변 등록")
        void success() {
            Review review = buildReview(1L);
            User trainerWithRole = trainerUserWithRole(10L, "트레이너");
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(userRepository.findById(10L)).willReturn(Optional.of(trainerWithRole));
            given(reviewReplyRepository.existsByReviewId(1L)).willReturn(false);
            given(reviewReplyRepository.save(any(ReviewReply.class))).willAnswer(inv -> {
                ReviewReply saved = inv.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 1L);
                return saved;
            });

            ReviewReplyResponseDto response = reviewService.createReply(1L, 10L, replyRequest("감사합니다"));

            assertThat(response.getContent()).isEqualTo("감사합니다");
            verify(reviewReplyRepository).save(any(ReviewReply.class));
        }

        @Test
        @DisplayName("실패 - 트레이너 권한이 없는 사용자")
        void fail_notTrainerRole() {
            Review review = buildReview(1L);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            assertThatThrownBy(() -> reviewService.createReply(1L, 1L, replyRequest("답변")))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("트레이너만");
        }

        @Test
        @DisplayName("실패 - 해당 리뷰의 담당 트레이너가 아님")
        void fail_notAssignedTrainer() {
            Review review = buildReview(1L);
            User otherTrainer = trainerUserWithRole(99L, "다른트레이너");
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(userRepository.findById(99L)).willReturn(Optional.of(otherTrainer));

            assertThatThrownBy(() -> reviewService.createReply(1L, 99L, replyRequest("답변")))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("담당 트레이너가 아닙니다");
        }

        @Test
        @DisplayName("실패 - 이미 답변이 등록된 리뷰")
        void fail_alreadyReplied() {
            Review review = buildReview(1L);
            User trainerWithRole = trainerUserWithRole(10L, "트레이너");
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(userRepository.findById(10L)).willReturn(Optional.of(trainerWithRole));
            given(reviewReplyRepository.existsByReviewId(1L)).willReturn(true);

            assertThatThrownBy(() -> reviewService.createReply(1L, 10L, replyRequest("답변")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("이미 답변");
        }
    }

    // ================================================================
    // 트레이너 답변 수정 테스트
    // ================================================================
    @Nested
    @DisplayName("트레이너 답변 수정 (updateReply)")
    class UpdateReply {

        @Test
        @DisplayName("성공 - 답변 작성자 본인이 수정")
        void success() {
            Review review = buildReview(1L);
            ReviewReply reply = buildReply(5L, review, trainer, "원래 답변");
            given(reviewReplyRepository.findByReviewId(1L)).willReturn(Optional.of(reply));

            ReviewReplyResponseDto response = reviewService.updateReply(1L, 10L, replyRequest("수정된 답변"));

            assertThat(response.getContent()).isEqualTo("수정된 답변");
        }

        @Test
        @DisplayName("실패 - 답변 작성자가 아닌 트레이너가 수정 시도")
        void fail_notOwner() {
            Review review = buildReview(1L);
            ReviewReply reply = buildReply(5L, review, trainer, "원래 답변");
            given(reviewReplyRepository.findByReviewId(1L)).willReturn(Optional.of(reply));

            assertThatThrownBy(() -> reviewService.updateReply(1L, 999L, replyRequest("수정")))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - 등록된 답변이 없음")
        void fail_notFound() {
            given(reviewReplyRepository.findByReviewId(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.updateReply(1L, 10L, replyRequest("수정")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ================================================================
    // 삭제 요청 / 신고 테스트
    // ================================================================
    @Nested
    @DisplayName("삭제 요청 / 신고 (reportReview)")
    class ReportReview {

        @Test
        @DisplayName("성공 - 담당 트레이너가 신고하면 리뷰가 즉시 숨김 처리됨")
        void success() {
            Review review = buildReview(1L);
            User trainerWithRole = trainerUserWithRole(10L, "트레이너");
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(userRepository.findById(10L)).willReturn(Optional.of(trainerWithRole));
            given(reviewReportRepository.existsByReviewIdAndStatus(1L, ReportStatus.PENDING)).willReturn(false);
            given(reviewReportRepository.save(any(ReviewReport.class))).willAnswer(inv -> {
                ReviewReport saved = inv.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 1L);
                return saved;
            });

            ReviewReportResponseDto response = reviewService.reportReview(1L, 10L, reportRequest("부적절한 내용"));

            assertThat(response.getReason()).isEqualTo("부적절한 내용");
            assertThat(review.getStatus()).isEqualTo(ReviewStatus.HIDDEN);
        }

        @Test
        @DisplayName("실패 - 트레이너 권한이 없는 사용자")
        void fail_notTrainerRole() {
            Review review = buildReview(1L);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            assertThatThrownBy(() -> reviewService.reportReview(1L, 1L, reportRequest("사유")))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("트레이너만");
        }

        @Test
        @DisplayName("실패 - 해당 리뷰의 담당 트레이너가 아님")
        void fail_notAssignedTrainer() {
            Review review = buildReview(1L);
            User otherTrainer = trainerUserWithRole(99L, "다른트레이너");
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(userRepository.findById(99L)).willReturn(Optional.of(otherTrainer));

            assertThatThrownBy(() -> reviewService.reportReview(1L, 99L, reportRequest("사유")))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("담당 트레이너가 아닙니다");
        }

        @Test
        @DisplayName("실패 - 이미 숨김/삭제된 리뷰")
        void fail_notVisible() {
            Review review = buildReview(1L);
            review.hide();
            User trainerWithRole = trainerUserWithRole(10L, "트레이너");
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(userRepository.findById(10L)).willReturn(Optional.of(trainerWithRole));

            assertThatThrownBy(() -> reviewService.reportReview(1L, 10L, reportRequest("사유")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("이미 숨김 또는 삭제된");
        }

        @Test
        @DisplayName("실패 - 이미 심사 대기 중인 신고가 있음")
        void fail_duplicatePending() {
            Review review = buildReview(1L);
            User trainerWithRole = trainerUserWithRole(10L, "트레이너");
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(userRepository.findById(10L)).willReturn(Optional.of(trainerWithRole));
            given(reviewReportRepository.existsByReviewIdAndStatus(1L, ReportStatus.PENDING)).willReturn(true);

            assertThatThrownBy(() -> reviewService.reportReview(1L, 10L, reportRequest("사유")))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("이미 심사 대기 중");
        }
    }

    // ================================================================
    // 관리자 심사 대기 목록 테스트
    // ================================================================
    @Nested
    @DisplayName("관리자 심사 대기 목록 조회 (getHiddenReviews)")
    class GetHiddenReviews {

        @Test
        @DisplayName("성공 - HIDDEN 상태 리뷰 목록 반환")
        void success() {
            Review review = buildReview(1L);
            review.hide();
            Pageable pageable = PageRequest.of(0, 10);
            given(reviewRepository.findByStatus(ReviewStatus.HIDDEN, pageable))
                    .willReturn(new PageImpl<>(List.of(review)));

            var result = reviewService.getHiddenReviews(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(ReviewStatus.HIDDEN);
        }
    }

    // ================================================================
    // 관리자 승인/반려 테스트
    // ================================================================
    @Nested
    @DisplayName("관리자 승인/반려 (decideReport)")
    class DecideReport {

        @Test
        @DisplayName("성공 - 승인(APPROVED) 시 리뷰가 ADMIN_DELETED로 전환")
        void success_approve() {
            Review review = buildReview(1L);
            review.hide();
            ReviewReport report = buildReport(1L, review, user, "사유");
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(reviewReportRepository.findByReviewIdAndStatus(1L, ReportStatus.PENDING))
                    .willReturn(Optional.of(report));

            ReviewResponseDto response = reviewService.decideReport(1L, decisionRequest(ReportStatus.APPROVED));

            assertThat(response.getStatus()).isEqualTo(ReviewStatus.ADMIN_DELETED);
            assertThat(report.getStatus()).isEqualTo(ReportStatus.APPROVED);
        }

        @Test
        @DisplayName("성공 - 반려(REJECTED) 시 리뷰가 VISIBLE로 복원")
        void success_reject() {
            Review review = buildReview(1L);
            review.hide();
            ReviewReport report = buildReport(1L, review, user, "사유");
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(reviewReportRepository.findByReviewIdAndStatus(1L, ReportStatus.PENDING))
                    .willReturn(Optional.of(report));

            ReviewResponseDto response = reviewService.decideReport(1L, decisionRequest(ReportStatus.REJECTED));

            assertThat(response.getStatus()).isEqualTo(ReviewStatus.VISIBLE);
            assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
        }

        @Test
        @DisplayName("실패 - HIDDEN 상태가 아닌 리뷰는 심사 불가")
        void fail_notHidden() {
            Review review = buildReview(1L);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            assertThatThrownBy(() -> reviewService.decideReport(1L, decisionRequest(ReportStatus.APPROVED)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("숨김 상태의 리뷰만");
        }

        @Test
        @DisplayName("실패 - 심사 대기 중인 신고가 없음")
        void fail_noPendingReport() {
            Review review = buildReview(1L);
            review.hide();
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(reviewReportRepository.findByReviewIdAndStatus(1L, ReportStatus.PENDING))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.decideReport(1L, decisionRequest(ReportStatus.APPROVED)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 심사 결정 값이 APPROVED/REJECTED가 아님")
        void fail_invalidDecision() {
            Review review = buildReview(1L);
            review.hide();
            ReviewReport report = buildReport(1L, review, user, "사유");
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(reviewReportRepository.findByReviewIdAndStatus(1L, ReportStatus.PENDING))
                    .willReturn(Optional.of(report));

            assertThatThrownBy(() -> reviewService.decideReport(1L, decisionRequest(ReportStatus.PENDING)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("APPROVED 또는 REJECTED");
        }
    }

    // ================================================================
    // 평점 집계 테스트
    // ================================================================
    @Nested
    @DisplayName("평점 집계 조회 (getTrainerRating / getProgramRating)")
    class RatingSummary {

        @Test
        @DisplayName("성공 - 트레이너 평점 집계 (리뷰 존재)")
        void trainerRating_withReviews() {
            given(reviewRepository.getTrainerRatingSummary(10L)).willReturn(new Object[]{4L, 4.5});

            RatingSummaryResponseDto response = reviewService.getTrainerRating(10L);

            assertThat(response.getReviewCount()).isEqualTo(4);
            assertThat(response.getAverageRating()).isEqualByComparingTo("4.50");
        }

        @Test
        @DisplayName("성공 - 트레이너 리뷰가 없으면 평점 0")
        void trainerRating_noReviews() {
            given(reviewRepository.getTrainerRatingSummary(10L)).willReturn(new Object[]{0L, 0.0});

            RatingSummaryResponseDto response = reviewService.getTrainerRating(10L);

            assertThat(response.getReviewCount()).isEqualTo(0);
            assertThat(response.getAverageRating()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("성공 - 프로그램 평점 집계 (리뷰 존재)")
        void programRating_withReviews() {
            given(reviewRepository.getProgramRatingSummary(100L)).willReturn(new Object[]{2L, 5.0});

            RatingSummaryResponseDto response = reviewService.getProgramRating(100L);

            assertThat(response.getReviewCount()).isEqualTo(2);
            assertThat(response.getAverageRating()).isEqualByComparingTo("5.00");
        }
    }
}
