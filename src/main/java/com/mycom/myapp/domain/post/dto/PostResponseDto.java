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
    private String writerName;
    private Category category;
    private String title;
    private String content;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .writerName(post.getWriter().getName())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .commentCount(post.getComments().size())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
