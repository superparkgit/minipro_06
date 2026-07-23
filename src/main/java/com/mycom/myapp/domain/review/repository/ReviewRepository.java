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

    // USER_DELETED가 아닌 상태의 리뷰가 존재하는지 확인 (재작성 가능 여부 판단용)
    boolean existsByReservationIdAndStatusNot(Long reservationId, ReviewStatus status);

    Optional<Review> findByReservationId(Long reservationId);

    @EntityGraph(attributePaths = {"reservation", "user", "program", "trainer"})
    Page<Review> findByProgramIdAndStatusIn(Long programId, List<ReviewStatus> statuses, Pageable pageable);

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
