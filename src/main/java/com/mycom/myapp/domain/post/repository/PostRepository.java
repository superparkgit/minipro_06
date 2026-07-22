package com.mycom.myapp.domain.post.repository;

import com.mycom.myapp.domain.post.entity.Category;
import com.mycom.myapp.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"writer"})
    Page<Post> findByCategory(Category category, Pageable pageable);

    @EntityGraph(attributePaths = {"writer"})
    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"writer"})
    Page<Post> findByCategoryAndTitleContaining(Category category, String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"writer"})
    Page<Post> findByWriterId(Long writerId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"writer"})
    Page<Post> findAll(Pageable pageable);
}
