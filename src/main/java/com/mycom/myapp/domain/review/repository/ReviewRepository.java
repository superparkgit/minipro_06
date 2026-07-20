package com.mycom.myapp.domain.review.repository;

import com.mycom.myapp.domain.review.entity.Review;
import com.mycom.myapp.domain.review.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByReservationId(Long reservationId);

    Optional<Review> findByReservationId(Long reservationId);

    Page<Review> findByProgramIdAndStatus(Long programId, ReviewStatus status, Pageable pageable);

    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    // 트레이너 평점 실시간 집계 (count, avg)
    @Query("SELECT COUNT(r), COALESCE(AVG(r.rating), 0) FROM Review r " +
            "WHERE r.trainer.id = :trainerId AND r.status = 'VISIBLE'")
    Object[] getTrainerRatingSummary(@Param("trainerId") Long trainerId);

    // 프로그램 평점 실시간 집계 (count, avg)
    @Query("SELECT COUNT(r), COALESCE(AVG(r.rating), 0) FROM Review r " +
            "WHERE r.program.id = :programId AND r.status = 'VISIBLE'")
    Object[] getProgramRatingSummary(@Param("programId") Long programId);
}
