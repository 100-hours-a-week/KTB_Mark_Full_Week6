package com.mark.community.repository;


import com.mark.community.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.deleted = false AND p.temp = false ORDER BY p.id DESC")
    List<Post> findPosts(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id < :lastPostId AND p.deleted = false AND p.temp = false ORDER BY p.id DESC")
    List<Post> findPosts(@Param("lastPostId") Long lastPostId, Pageable pageable);

}
