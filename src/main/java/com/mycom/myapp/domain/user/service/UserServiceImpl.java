package com.mycom.myapp.domain.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.global.exception.ResourceNotFoundException;
import com.mycom.myapp.domain.user.dto.response.MyProfileResponse;
import com.mycom.myapp.domain.user.dto.response.TrainerSummaryResponse;
import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * 로그인한 회원의 PK로 회원과 역할 정보를 조회.
     */
    @Override
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("회원", userId)
                );

        return MyProfileResponse.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerSummaryResponse> getTrainers() {
        return userRepository.findAllByRole(Role.ROLE_TRAINER).stream()
                .map(TrainerSummaryResponse::from)
                .toList();
    }
}
