package com.mycom.myapp.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // TODO: 이용재 - 필요한 쿼리 메서드 추가
    // 특정 게시글의 댓글 목록
    // List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
