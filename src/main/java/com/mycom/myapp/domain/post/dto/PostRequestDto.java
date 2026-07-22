package com.mycom.myapp.domain.post.dto;

import com.mycom.myapp.domain.post.entity.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class PostRequestDto {
    @NotNull(message = "카테고리를 선택해주세요.")
    private Category category;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(min = 2, max = 100, message = "제목은 2자 이상, 100자 이하로 작성해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Size(min = 5, max = 10000, message = "내용은 5자 이상, 10000자 이하로 작성해주세요.")
    private String content;
}
