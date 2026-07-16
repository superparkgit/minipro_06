package com.mycom.myapp.domain.comment.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.comment.entity.Comment;

public record CommentResponse(
    Long id,
    Long postId,
    Long userId,
    String userName,
    String content,
    LocalDateTime createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getPost().getId(),
            comment.getUser().getId(),
            comment.getUser().getName(),
            comment.getContent(),
            comment.getCreatedAt()
        );
    }
}
