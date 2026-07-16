package com.mycom.myapp.domain.post.dto;

import com.mycom.myapp.domain.post.entity.Post.PostCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostRequest(

    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    String content,

    @NotNull(message = "카테고리는 필수입니다.")
    PostCategory category
) {}
