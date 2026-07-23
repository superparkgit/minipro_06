package com.mycom.myapp.domain.user.service;

import java.util.List;

import com.mycom.myapp.domain.user.dto.response.MyProfileResponse;
import com.mycom.myapp.domain.user.dto.response.TrainerSummaryResponse;

/**
 * 로그인한 회원이 자신의 정보를 조회할 때 사용.
 */
public interface UserService {

    /**
     * 회원 PK로 현재 로그인한 회원 정보를 조회.
     */
    MyProfileResponse getMyProfile(Long userId);

    /**
     * 보조 트레이너 선택에 사용할 전체 트레이너 목록을 조회.
     */
    List<TrainerSummaryResponse> getTrainers();
}
