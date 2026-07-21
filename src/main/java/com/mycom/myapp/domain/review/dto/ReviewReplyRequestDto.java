package com.mycom.myapp.domain.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class ReviewReplyRequestDto {
    @NotBlank(message = "답변 내용을 입력해주세요.")
    @Size(max = 500, message = "답변은 500자 이하로 작성해주세요.")
    private String content;
}
