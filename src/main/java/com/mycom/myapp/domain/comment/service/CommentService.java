package com.mycom.myapp.domain.comment.service;

import com.mycom.myapp.domain.comment.dto.CommentRequestDto;
import com.mycom.myapp.domain.comment.dto.CommentResponseDto;
import com.mycom.myapp.domain.comment.entity.Comment;
import com.mycom.myapp.domain.comment.repository.CommentRepository;
import com.mycom.myapp.domain.global.exception.AccessDeniedException;
import com.mycom.myapp.domain.global.exception.ResourceNotFoundException;
import com.mycom.myapp.domain.post.entity.Post;
import com.mycom.myapp.domain.post.repository.PostRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponseDto createComment(Long postId, Long writerId, CommentRequestDto requestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글", postId));

        User writer = userRepository.findById(writerId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", writerId));

        Comment comment = Comment.builder()
                .post(post)
                .writer(writer)
                .content(requestDto.getContent())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentResponseDto.from(savedComment);
    }

    public List<CommentResponseDto> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(CommentResponseDto::from)
                .toList();
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, Long writerId, CommentRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글", commentId));

        if (!comment.getWriter().getId().equals(writerId)) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        comment.update(requestDto.getContent());
        return CommentResponseDto.from(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long writerId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글", commentId));

        if (!comment.getWriter().getId().equals(writerId)) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}
