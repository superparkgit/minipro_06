package com.mycom.myapp.domain.comment.repository;

import com.mycom.myapp.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    @EntityGraph(attributePaths = {"writer"})
    Page<Comment> findByPostId(Long postId, Pageable pageable);
}
