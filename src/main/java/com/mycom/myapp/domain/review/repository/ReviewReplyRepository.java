package com.mycom.myapp.domain.review.repository;

import com.mycom.myapp.domain.review.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {

    Optional<ReviewReply> findByReviewId(Long reviewId);

    boolean existsByReviewId(Long reviewId);
}
