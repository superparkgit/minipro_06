package com.mycom.myapp.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequestDto {
    private int rating;

    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    @Size(min = 10, max = 500, message = "리뷰는 10자 이상, 500자 이하로 작성해주세요.")
    private String content;
}
