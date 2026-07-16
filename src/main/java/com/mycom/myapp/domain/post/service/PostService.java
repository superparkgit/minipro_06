package com.mycom.myapp.domain.post.service;

import com.mycom.myapp.domain.post.dto.PostResponseDto;
import com.mycom.myapp.domain.post.entity.Category;
import com.mycom.myapp.domain.post.repository.PostRepository;
import com.mycom.myapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<PostResponseDto> getPostByCategory(Category category){
        return postRepository
                .findByCategoryOrderByCreatedAtDesc(category).stream()
                .map(PostResponseDto::from).toList();
    }

    public List<PostResponseDto> getAllPosts(){
        return postRepository
                .findAllByOrderByCreatedAtDesc().stream()
                .map(PostResponseDto::from).toList();
    }

}
