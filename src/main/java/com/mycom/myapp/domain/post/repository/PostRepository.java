package com.mycom.myapp.domain.post.repository;

import com.mycom.myapp.domain.post.entity.Category;
import com.mycom.myapp.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByCategoryOrderByCreatedAtDesc(Category category);
    List<Post> findAllByOrderByCreatedAtDesc();
}
