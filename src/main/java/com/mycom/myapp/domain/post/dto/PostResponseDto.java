package com.mycom.myapp.domain.post.dto;

import com.mycom.myapp.domain.post.entity.Category;
import com.mycom.myapp.domain.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponseDto {
    private Long id;
    private Long writerId;
    private String writerName;
    private Category category;
    private String title;
    private String content;
    private int viewCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponseDto from(Post post, long commentCount) {
        return PostResponseDto.builder()
                .id(post.getId())
                .writerId(post.getWriter().getId())
                .writerName(post.getWriter().getName())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .commentCount((int) commentCount)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
