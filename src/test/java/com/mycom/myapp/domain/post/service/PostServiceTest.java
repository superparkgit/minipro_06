package com.mycom.myapp.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.mycom.myapp.domain.comment.repository.CommentRepository;
import com.mycom.myapp.domain.global.exception.AccessDeniedException;
import com.mycom.myapp.domain.global.exception.ResourceNotFoundException;
import com.mycom.myapp.domain.post.dto.PostRequestDto;
import com.mycom.myapp.domain.post.dto.PostResponseDto;
import com.mycom.myapp.domain.post.entity.Category;
import com.mycom.myapp.domain.post.entity.Post;
import com.mycom.myapp.domain.post.repository.PostRepository;
import com.mycom.myapp.domain.user.entity.Role;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.entity.UserRole;
import com.mycom.myapp.domain.user.repository.UserRepository;

/**
 * PostService 단위 테스트 (Mockito)
 * - 게시글 CRUD 비즈니스 로직을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommentRepository commentRepository;

    @InjectMocks
    private PostService postService;

    private User writer;

    @BeforeEach
    void setUp() {
        writer = User.builder().email("user@test.com").password("pw").name("회원").build();
        ReflectionTestUtils.setField(writer, "id", 1L);
    }

    private User adminUser(Long id) {
        UserRole adminRole;
        try {
            var constructor = UserRole.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            adminRole = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(adminRole, "roleName", Role.ROLE_ADMIN);

        User admin = User.builder()
                .email("admin@test.com").password("pw").name("관리자")
                .userRoles(java.util.Set.of(adminRole))
                .build();
        ReflectionTestUtils.setField(admin, "id", id);
        return admin;
    }

    private PostRequestDto postRequest(Category category, String title, String content) {
        PostRequestDto dto = new PostRequestDto();
        ReflectionTestUtils.setField(dto, "category", category);
        ReflectionTestUtils.setField(dto, "title", title);
        ReflectionTestUtils.setField(dto, "content", content);
        return dto;
    }

    private Post buildPost(Long id, User writer, Category category, String title, String content) {
        Post post = Post.builder()
                .writer(writer)
                .category(category)
                .title(title)
                .content(content)
                .build();
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    // ================================================================
    // 게시글 작성 테스트
    // ================================================================
    @Nested
    @DisplayName("게시글 작성 (createPost)")
    class CreatePost {

        @Test
        @DisplayName("성공 - QUESTION 카테고리 게시글 작성")
        void success_question() {
            given(userRepository.findById(1L)).willReturn(Optional.of(writer));
            given(postRepository.save(any(Post.class))).willAnswer(inv -> {
                Post saved = inv.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 10L);
                return saved;
            });

            PostResponseDto response = postService.createPost(1L, postRequest(Category.QUESTION, "질문입니다", "궁금한 점이 있습니다"));

            assertThat(response.getTitle()).isEqualTo("질문입니다");
            assertThat(response.getCategory()).isEqualTo(Category.QUESTION);
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void fail_userNotFound() {
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.createPost(999L, postRequest(Category.QUESTION, "질문", "내용")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 일반 회원이 NOTICE 카테고리 게시글 작성")
        void fail_noticeByNonAdmin() {
            given(userRepository.findById(1L)).willReturn(Optional.of(writer));

            assertThatThrownBy(() -> postService.createPost(1L, postRequest(Category.NOTICE, "공지", "공지 내용")))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("관리자");
        }

        @Test
        @DisplayName("성공 - ADMIN이 NOTICE 카테고리 게시글 작성")
        void success_noticeByAdmin() {
            User admin = adminUser(2L);
            given(userRepository.findById(2L)).willReturn(Optional.of(admin));
            given(postRepository.save(any(Post.class))).willAnswer(inv -> {
                Post saved = inv.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 20L);
                return saved;
            });

            PostResponseDto response = postService.createPost(2L, postRequest(Category.NOTICE, "공지사항", "공지 내용"));

            assertThat(response.getCategory()).isEqualTo(Category.NOTICE);
            verify(postRepository).save(any(Post.class));
        }
    }

    // ================================================================
    // 게시글 상세 조회 테스트
    // ================================================================
    @Nested
    @DisplayName("게시글 상세 조회 (getPostById)")
    class GetPostById {

        @Test
        @DisplayName("성공 - 존재하는 게시글 조회")
        void success() {
            Post post = buildPost(10L, writer, Category.QUESTION, "질문글", "내용");
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            PostResponseDto response = postService.getPostById(10L, false);

            assertThat(response.getId()).isEqualTo(10L);
            assertThat(response.getTitle()).isEqualTo("질문글");
            assertThat(response.getWriterName()).isEqualTo("회원");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void fail_notFound() {
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getPostById(999L, false))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ================================================================
    // 게시글 수정 테스트
    // ================================================================
    @Nested
    @DisplayName("게시글 수정 (updatePost)")
    class UpdatePost {

        @Test
        @DisplayName("성공 - 작성자 본인이 수정")
        void success() {
            Post post = buildPost(10L, writer, Category.QUESTION, "원래 제목", "원래 내용");
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            PostResponseDto response = postService.updatePost(10L, 1L,
                    postRequest(Category.QUESTION, "수정된 제목", "수정된 내용"));

            assertThat(response.getTitle()).isEqualTo("수정된 제목");
            assertThat(response.getContent()).isEqualTo("수정된 내용");
            assertThat(response.getCategory()).isEqualTo(Category.QUESTION);
        }

        @Test
        @DisplayName("실패 - 다른 사용자가 수정 시도")
        void fail_notOwner() {
            Post post = buildPost(10L, writer, Category.QUESTION, "제목", "내용");
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.updatePost(10L, 999L,
                    postRequest(Category.QUESTION, "수정", "수정")))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void fail_notFound() {
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.updatePost(999L, 1L,
                    postRequest(Category.QUESTION, "수정", "수정")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 일반 회원이 NOTICE로 카테고리 변경 시도")
        void fail_updateToNoticeByNonAdmin() {
            Post post = buildPost(10L, writer, Category.QUESTION, "제목", "내용");
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.updatePost(10L, 1L,
                    postRequest(Category.NOTICE, "공지로 변경", "내용")))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("관리자");
        }
    }

    // ================================================================
    // 게시글 삭제 테스트
    // ================================================================
    @Nested
    @DisplayName("게시글 삭제 (deletePost)")
    class DeletePost {

        @Test
        @DisplayName("성공 - 작성자 본인이 삭제")
        void success() {
            Post post = buildPost(10L, writer, Category.QUESTION, "제목", "내용");
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            postService.deletePost(10L, 1L);

            verify(commentRepository).deleteByPostId(10L);
            verify(postRepository).delete(post);
        }

        @Test
        @DisplayName("실패 - 다른 사용자가 삭제 시도")
        void fail_notOwner() {
            Post post = buildPost(10L, writer, Category.QUESTION, "제목", "내용");
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.deletePost(10L, 999L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void fail_notFound() {
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.deletePost(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ================================================================
    // 게시글 목록 조회 테스트
    // ================================================================
    @Nested
    @DisplayName("게시글 목록 조회 (getPosts)")
    class GetPosts {

        @Test
        @DisplayName("성공 - 카테고리와 키워드 없이 전체 조회")
        void success_noFilter() {
            Post post = buildPost(10L, writer, Category.QUESTION, "제목", "내용");
            Pageable pageable = PageRequest.of(0, 10);
            given(postRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(post)));

            Page<PostResponseDto> result = postService.getPosts(null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(postRepository).findAll(pageable);
        }

        @Test
        @DisplayName("성공 - 카테고리만 지정")
        void success_categoryOnly() {
            Post post = buildPost(10L, writer, Category.QUESTION, "제목", "내용");
            Pageable pageable = PageRequest.of(0, 10);
            given(postRepository.findByCategory(Category.QUESTION, pageable))
                    .willReturn(new PageImpl<>(List.of(post)));

            Page<PostResponseDto> result = postService.getPosts(Category.QUESTION, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(postRepository).findByCategory(Category.QUESTION, pageable);
            verify(postRepository, never()).findAll(pageable);
        }

        @Test
        @DisplayName("성공 - 키워드만 지정")
        void success_keywordOnly() {
            Post post = buildPost(10L, writer, Category.QUESTION, "질문 제목", "내용");
            Pageable pageable = PageRequest.of(0, 10);
            given(postRepository.findByTitleContaining("질문", pageable))
                    .willReturn(new PageImpl<>(List.of(post)));

            Page<PostResponseDto> result = postService.getPosts(null, "질문", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(postRepository).findByTitleContaining("질문", pageable);
        }

        @Test
        @DisplayName("성공 - 카테고리와 키워드 둘 다 지정")
        void success_categoryAndKeyword() {
            Post post = buildPost(10L, writer, Category.QUESTION, "질문 제목", "내용");
            Pageable pageable = PageRequest.of(0, 10);
            given(postRepository.findByCategoryAndTitleContaining(Category.QUESTION, "질문", pageable))
                    .willReturn(new PageImpl<>(List.of(post)));

            Page<PostResponseDto> result = postService.getPosts(Category.QUESTION, "질문", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(postRepository).findByCategoryAndTitleContaining(Category.QUESTION, "질문", pageable);
        }

        @Test
        @DisplayName("성공 - 공백만 있는 키워드는 미지정으로 취급")
        void success_blankKeywordTreatedAsNoFilter() {
            Post post = buildPost(10L, writer, Category.QUESTION, "제목", "내용");
            Pageable pageable = PageRequest.of(0, 10);
            given(postRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(post)));

            Page<PostResponseDto> result = postService.getPosts(null, "   ", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(postRepository).findAll(pageable);
        }

        @Test
        @DisplayName("성공 - 댓글 수가 배치 조회 결과로 채워짐")
        void success_commentCountBatched() {
            Post post1 = buildPost(10L, writer, Category.QUESTION, "제목1", "내용1");
            Post post2 = buildPost(11L, writer, Category.QUESTION, "제목2", "내용2");
            Pageable pageable = PageRequest.of(0, 10);
            given(postRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(post1, post2)));
            given(commentRepository.countByPostIdIn(List.of(10L, 11L)))
                    .willReturn(java.util.Collections.singletonList(new Object[]{10L, 3L}));

            Page<PostResponseDto> result = postService.getPosts(null, null, pageable);

            assertThat(result.getContent().get(0).getCommentCount()).isEqualTo(3);
            assertThat(result.getContent().get(1).getCommentCount()).isEqualTo(0);
        }
    }

    // ================================================================
    // 작성자별 게시글 목록 조회 테스트
    // ================================================================
    @Nested
    @DisplayName("작성자별 게시글 목록 조회 (getPostsByWriter)")
    class GetPostsByWriter {

        @Test
        @DisplayName("성공 - 작성자 ID로 게시글 목록 조회")
        void success() {
            Post post = buildPost(10L, writer, Category.QUESTION, "내 글", "내용");
            Pageable pageable = PageRequest.of(0, 10);
            given(postRepository.findByWriterId(1L, pageable)).willReturn(new PageImpl<>(List.of(post)));

            Page<PostResponseDto> result = postService.getPostsByWriter(1L, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getWriterId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("성공 - 작성한 글이 없으면 빈 페이지 반환")
        void success_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            given(postRepository.findByWriterId(1L, pageable)).willReturn(new PageImpl<>(List.of()));

            Page<PostResponseDto> result = postService.getPostsByWriter(1L, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }
}
