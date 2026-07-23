package com.mycom.myapp.domain.user.dto.response;

import com.mycom.myapp.domain.user.entity.User;

/**
 * 프로그램 담당자 선택 화면에 필요한 최소 트레이너 정보.
 */
public record TrainerSummaryResponse(
        Long id,
        String name
) {
    public static TrainerSummaryResponse from(User user) {
        return new TrainerSummaryResponse(user.getId(), user.getName());
    }
}
