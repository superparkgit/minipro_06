package com.mycom.myapp.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("트레이너 역할 회원을 이름순 목록으로 변환한다")
    void getTrainers_success() {
        User trainer = User.builder()
                .email("trainer@test.com")
                .password("password")
                .name("김트레이너")
                .build();
        ReflectionTestUtils.setField(trainer, "id", 2L);
        given(userRepository.findAllByRole(Role.ROLE_TRAINER)).willReturn(List.of(trainer));

        var result = userService.getTrainers();

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(2L);
            assertThat(item.name()).isEqualTo("김트레이너");
        });
    }
}
