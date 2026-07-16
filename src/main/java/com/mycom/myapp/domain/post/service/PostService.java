package com.mycom.myapp.domain.post.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.comment.repository.CommentRepository;
import com.mycom.myapp.domain.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

/**
 * PostService - 이용재 담당
 * <p>
 * TODO: 다음 API 구현
 * - getPosts(category, pageable): 게시글 목록 (카테고리/페이징)
 * - getPost(id): 게시글 상세
 * - createPost(request, userId): 게시글 작성
 * - updatePost(id, request, userId): 게시글 수정 (작성자 본인 확인)
 * - deletePost(id, userId): 게시글 삭제 (작성자 본인 확인)
 * - createComment(postId, request, userId): 댓글 작성
 * - deleteComment(commentId, userId): 댓글 삭제 (작성자 본인 확인)
 * - getMyPosts(userId, pageable): 내가 쓴 글
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // TODO: 이용재 - 서비스 메서드 구현
}
