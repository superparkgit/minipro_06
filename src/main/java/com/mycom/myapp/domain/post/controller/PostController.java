package com.mycom.myapp.domain.post.controller;

import com.mycom.myapp.domain.post.service.PostService;
import com.mycom.myapp.domain.post.dto.PostResponseDto;
import com.mycom.myapp.domain.post.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getPost(@RequestParam(required = false) Category category){
        List<PostResponseDto> response;
        if(category != null){
            response = postService.getPostByCategory(category);
        }else {
            response = postService.getAllPosts();
        }
        return ResponseEntity.ok(response);
    }
}
