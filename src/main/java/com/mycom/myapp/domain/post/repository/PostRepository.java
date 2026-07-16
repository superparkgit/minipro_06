package com.mycom.myapp.domain.post.repository;

import com.mycom.myapp.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
