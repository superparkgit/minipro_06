package com.mycom.myapp.domain.review.repository;

import com.mycom.myapp.domain.review.entity.Review;
import com.mycom.myapp.domain.review.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByReservationId(Long reservationId);

    // 예약 목록에서 각 예약에 달린(재작성 가능한 USER_DELETED 상태 제외) 리뷰를 한 번에 조회
    List<Review> findByReservationIdInAndStatusNot(List<Long> reservationIds, ReviewStatus status);

    @EntityGraph(attributePaths = {"reservation", "user", "program", "trainer"})
    Page<Review> findByProgramIdAndStatusIn(Long programId, List<ReviewStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = {"reservation", "user", "program", "trainer"})
    Page<Review> findByTrainerIdAndStatusIn(Long trainerId, List<ReviewStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = {"reservation", "user", "program", "trainer"})
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    // 트레이너 평점 실시간 집계 (count, avg)
    @Query("SELECT COUNT(r), COALESCE(AVG(r.rating), 0) FROM Review r " +
            "WHERE r.trainer.id = :trainerId AND r.status = 'VISIBLE'")
    List<Object[]> getTrainerRatingSummary(@Param("trainerId") Long trainerId);

    // 프로그램 평점 실시간 집계 (count, avg)
    @Query("SELECT COUNT(r), COALESCE(AVG(r.rating), 0) FROM Review r " +
            "WHERE r.program.id = :programId AND r.status = 'VISIBLE'")
    List<Object[]> getProgramRatingSummary(@Param("programId") Long programId);
}
