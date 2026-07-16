package com.mycom.myapp.domain.post.repository;

import com.mycom.myapp.domain.post.entity.Category;
import com.mycom.myapp.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByCategory(Category category, Pageable pageable);
    Page<Post> findByTitleContaining(String keyword, Pageable pageable);
    Page<Post> findByCategoryAndTitleContaining(Category category, String keyword, Pageable pageable);
    Page<Post> findByWriterId(Long writerId, Pageable pageable);
}
