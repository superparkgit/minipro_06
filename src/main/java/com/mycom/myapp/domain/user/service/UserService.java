package com.mycom.myapp.domain.user.service;

import com.mycom.myapp.domain.user.dto.response.MyProfileResponse;

/**
 * 로그인한 회원이 자신의 정보를 조회할 때 사용.
 */
public interface UserService {

    /**
     * 회원 PK로 현재 로그인한 회원 정보를 조회.
     */
    MyProfileResponse getMyProfile(Long userId);
}
