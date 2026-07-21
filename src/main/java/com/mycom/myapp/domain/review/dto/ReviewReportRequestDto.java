package com.mycom.myapp.domain.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class ReviewReportRequestDto {
    @NotBlank(message = "신고 사유를 입력해주세요.")
    @Size(max = 200, message = "신고 사유는 200자 이하로 작성해주세요.")
    private String reason;
}
