package com.mycom.myapp.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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

import com.mycom.myapp.domain.comment.dto.CommentRequestDto;
import com.mycom.myapp.domain.comment.dto.CommentResponseDto;
import com.mycom.myapp.domain.comment.entity.Comment;
import com.mycom.myapp.domain.comment.repository.CommentRepository;
import com.mycom.myapp.domain.global.exception.AccessDeniedException;
import com.mycom.myapp.domain.global.exception.ResourceNotFoundException;
import com.mycom.myapp.domain.post.entity.Category;
import com.mycom.myapp.domain.post.entity.Post;
import com.mycom.myapp.domain.post.repository.PostRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;

/**
 * CommentService 단위 테스트 (Mockito)
 * - 댓글 CRUD 비즈니스 로직을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User writer;
    private Post post;

    @BeforeEach
    void setUp() {
        writer = User.builder().email("user@test.com").password("pw").name("회원").build();
        ReflectionTestUtils.setField(writer, "id", 1L);

        post = Post.builder()
                .writer(writer)
                .category(Category.QUESTION)
                .title("질문글")
                .content("질문 내용입니다")
                .build();
        ReflectionTestUtils.setField(post, "id", 10L);
    }

    private CommentRequestDto commentRequest(String content) {
        CommentRequestDto dto = new CommentRequestDto();
        ReflectionTestUtils.setField(dto, "content", content);
        return dto;
    }

    private Comment buildComment(Long id, User writer, Post post, String content) {
        Comment comment = Comment.builder()
                .writer(writer)
                .post(post)
                .content(content)
                .build();
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }

    // ================================================================
    // 댓글 작성 테스트
    // ================================================================
    @Nested
    @DisplayName("댓글 작성 (createComment)")
    class CreateComment {

        @Test
        @DisplayName("성공 - 게시글에 댓글 작성")
        void success() {
            given(postRepository.findById(10L)).willReturn(Optional.of(post));
            given(userRepository.findById(1L)).willReturn(Optional.of(writer));
            given(commentRepository.save(any(Comment.class))).willAnswer(inv -> {
                Comment saved = inv.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 100L);
                return saved;
            });

            CommentResponseDto response = commentService.createComment(10L, 1L, commentRequest("댓글 내용"));

            assertThat(response.getContent()).isEqualTo("댓글 내용");
            assertThat(response.getPostId()).isEqualTo(10L);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글에 댓글 작성")
        void fail_postNotFound() {
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.createComment(999L, 1L, commentRequest("댓글")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void fail_userNotFound() {
            given(postRepository.findById(10L)).willReturn(Optional.of(post));
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.createComment(10L, 999L, commentRequest("댓글")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ================================================================
    // 댓글 목록 조회 테스트
    // ================================================================
    @Nested
    @DisplayName("댓글 목록 조회 (getCommentsByPost)")
    class GetCommentsByPost {

        @Test
        @DisplayName("성공 - 작성일 순으로 정렬되어 반환")
        void success() {
            Comment c1 = buildComment(1L, writer, post, "첫번째");
            Comment c2 = buildComment(2L, writer, post, "두번째");

            given(postRepository.existsById(10L)).willReturn(true);
            given(commentRepository.findByPostId(eq(10L), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(c1, c2)));

            Page<CommentResponseDto> result = commentService.getCommentsByPost(10L, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getContent()).isEqualTo("첫번째");
            assertThat(result.getContent().get(1).getContent()).isEqualTo("두번째");
        }

        @Test
        @DisplayName("성공 - 빈 목록 반환")
        void success_empty() {
            given(postRepository.existsById(10L)).willReturn(true);
            given(commentRepository.findByPostId(eq(10L), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of()));

            Page<CommentResponseDto> result = commentService.getCommentsByPost(10L, PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void fail_postNotFound() {
            given(postRepository.existsById(999L)).willReturn(false);

            assertThatThrownBy(() -> commentService.getCommentsByPost(999L, PageRequest.of(0, 10)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ================================================================
    // 댓글 수정 테스트
    // ================================================================
    @Nested
    @DisplayName("댓글 수정 (updateComment)")
    class UpdateComment {

        @Test
        @DisplayName("성공 - 작성자 본인이 수정")
        void success() {
            Comment comment = buildComment(100L, writer, post, "원래 댓글");
            given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

            CommentResponseDto response = commentService.updateComment(100L, 1L, commentRequest("수정된 댓글"));

            assertThat(response.getContent()).isEqualTo("수정된 댓글");
        }

        @Test
        @DisplayName("실패 - 다른 사용자가 수정 시도")
        void fail_notOwner() {
            Comment comment = buildComment(100L, writer, post, "원래 댓글");
            given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.updateComment(100L, 999L, commentRequest("수정")))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 댓글")
        void fail_notFound() {
            given(commentRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.updateComment(999L, 1L, commentRequest("수정")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ================================================================
    // 댓글 삭제 테스트
    // ================================================================
    @Nested
    @DisplayName("댓글 삭제 (deleteComment)")
    class DeleteComment {

        @Test
        @DisplayName("성공 - 작성자 본인이 삭제")
        void success() {
            Comment comment = buildComment(100L, writer, post, "삭제할 댓글");
            given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

            commentService.deleteComment(100L, 1L);

            verify(commentRepository).delete(comment);
        }

        @Test
        @DisplayName("실패 - 다른 사용자가 삭제 시도")
        void fail_notOwner() {
            Comment comment = buildComment(100L, writer, post, "삭제할 댓글");
            given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.deleteComment(100L, 999L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 댓글")
        void fail_notFound() {
            given(commentRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.deleteComment(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
