package com.mycom.myapp.domain.post.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.post.service.PostService;

import lombok.RequiredArgsConstructor;

/**
 * PostController - 이용재 담당
 * <p>
 * TODO: roadmap.md C. 게시판 API 구현
 * - GET    /api/posts               : 게시글 목록 (카테고리/페이징)
 * - GET    /api/posts/{id}          : 게시글 상세
 * - POST   /api/posts               : 게시글 작성
 * - PATCH  /api/posts/{id}          : 게시글 수정 (작성자)
 * - DELETE /api/posts/{id}          : 게시글 삭제 (작성자)
 * - POST   /api/posts/{id}/comments : 댓글 작성
 * - DELETE /api/comments/{id}       : 댓글 삭제 (작성자)
 * - GET    /api/users/me/posts      : 내가 쓴 글
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // TODO: 이용재 - 컨트롤러 메서드 구현
}
