package com.mycom.myapp.domain.post.controller;

import com.mycom.myapp.domain.post.dto.PostRequestDto;
import com.mycom.myapp.domain.post.service.PostService;
import com.mycom.myapp.domain.post.dto.PostResponseDto;
import com.mycom.myapp.domain.post.entity.Category;
import com.mycom.myapp.domain.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPosts(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<PostResponseDto> response = postService.getPosts(category, keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostRequestDto requestDto
    ){
        PostResponseDto response = postService.createPost(userDetails.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> detailPost(
            @PathVariable Long postId,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        boolean shouldIncreaseViewCount = true;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("viewed_post_" + postId)) {
                    shouldIncreaseViewCount = false;
                    break;
                }
            }
        }
        
        if (shouldIncreaseViewCount) {
            Cookie newCookie = new Cookie("viewed_post_" + postId, "true");
            newCookie.setMaxAge(60 * 60 * 24); // 24 hours
            newCookie.setPath("/api/posts/" + postId);
            response.addCookie(newCookie);
        }

        PostResponseDto responseDto = postService.getPostById(postId, shouldIncreaseViewCount);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/me")
    public ResponseEntity<Page<PostResponseDto>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponseDto> response = postService.getPostsByWriter(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostRequestDto requestDto) {
        PostResponseDto response = postService.updatePost(postId, userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.deletePost(postId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
