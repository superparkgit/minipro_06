package com.mycom.myapp.domain.review.repository;

import com.mycom.myapp.domain.review.entity.ReportStatus;
import com.mycom.myapp.domain.review.entity.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

    boolean existsByReviewIdAndStatus(Long reviewId, ReportStatus status);

    Optional<ReviewReport> findByReviewIdAndStatus(Long reviewId, ReportStatus status);

    List<ReviewReport> findByReviewIdInAndStatus(List<Long> reviewIds, ReportStatus status);
}
