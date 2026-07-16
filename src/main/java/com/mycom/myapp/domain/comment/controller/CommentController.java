package com.mycom.myapp.domain.comment.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.post.service.PostService;

import lombok.RequiredArgsConstructor;

/**
 * CommentController - 이용재 담당
 * <p>
 * TODO:
 * - DELETE /api/comments/{id} : 댓글 삭제 (작성자)
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final PostService postService;

    // TODO: 이용재 - 컨트롤러 메서드 구현
}
