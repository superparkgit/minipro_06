package com.mycom.myapp.domain.post.service;

import com.mycom.myapp.domain.post.dto.PostRequestDto;
import com.mycom.myapp.domain.post.dto.PostResponseDto;
import com.mycom.myapp.domain.post.entity.Category;
import com.mycom.myapp.domain.post.entity.Post;
import com.mycom.myapp.domain.post.repository.PostRepository;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Page<PostResponseDto> getPosts(Category category, String keyword, Pageable pageable) {
        Page<Post> posts;

        boolean hasCategory = category != null;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasCategory && hasKeyword) {
            posts = postRepository.findByCategoryAndTitleContaining(category, keyword, pageable);
        } else if (hasCategory) {
            posts = postRepository.findByCategory(category, pageable);
        } else if (hasKeyword) {
            posts = postRepository.findByTitleContaining(keyword, pageable);
        } else {
            posts = postRepository.findAll(pageable);
        }

        return posts.map(PostResponseDto::from);
    }

    @Transactional
    public PostResponseDto createPost(Long writerId, PostRequestDto requestDto) {
        User writer = userRepository.findById(writerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + writerId));

        Post post = Post.builder()
                .writer(writer)
                .category(requestDto.getCategory())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .build();

        Post savedPost = postRepository.save(post);
        return PostResponseDto.from(savedPost);
    }

    public PostResponseDto getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다. id=" + postId));
        return PostResponseDto.from(post);
    }

    public Page<PostResponseDto> getPostsByWriter(Long writerId, Pageable pageable) {
        return postRepository.findByWriterId(writerId, pageable)
                .map(PostResponseDto::from);
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, Long writerId, PostRequestDto requestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다. id=" + postId));

        if (!post.getWriter().getId().equals(writerId)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        post.update(requestDto.getTitle(), requestDto.getContent(), requestDto.getCategory());
        return PostResponseDto.from(post);
    }

    @Transactional
    public void deletePost(Long postId, Long writerId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다. id=" + postId));

        if (!post.getWriter().getId().equals(writerId)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }
}
