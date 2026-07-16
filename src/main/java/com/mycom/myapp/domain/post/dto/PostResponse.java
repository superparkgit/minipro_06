package com.mycom.myapp.domain.post.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.post.entity.Post;
import com.mycom.myapp.domain.post.entity.Post.PostCategory;

public record PostResponse(
    Long id,
    Long userId,
    String userName,
    String title,
    String content,
    PostCategory category,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
            post.getId(),
            post.getUser().getId(),
            post.getUser().getName(),
            post.getTitle(),
            post.getContent(),
            post.getCategory(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }
}
