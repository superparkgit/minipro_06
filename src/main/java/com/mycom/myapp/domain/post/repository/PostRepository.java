package com.mycom.myapp.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.post.entity.Post;
import com.mycom.myapp.domain.post.entity.Post.PostCategory;

public interface PostRepository extends JpaRepository<Post, Long> {
    // TODO: 이용재 - 필요한 쿼리 메서드 추가
    // 카테고리 필터 + 페이징
    Page<Post> findByCategory(PostCategory category, Pageable pageable);
    // 내가 쓴 글
    Page<Post> findByUserId(Long userId, Pageable pageable);
}
