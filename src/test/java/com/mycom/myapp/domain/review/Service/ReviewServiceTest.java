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
import com.mycom.myapp.domain.review.dto.ReviewRequestDto;
import com.mycom.myapp.domain.review.dto.ReviewResponseDto;
import com.mycom.myapp.domain.review.entity.Review;
import com.mycom.myapp.domain.review.entity.ReviewStatus;
import com.mycom.myapp.domain.review.repository.ReviewReplyRepository;
import com.mycom.myapp.domain.review.repository.ReviewReportRepository;
import com.mycom.myapp.domain.review.repository.ReviewRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
}
