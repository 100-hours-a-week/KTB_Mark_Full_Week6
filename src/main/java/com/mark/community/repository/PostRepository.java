package com.mark.community.repository;


import com.mark.community.entity.Post;
import com.mark.community.enums.PostCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.deleted = false AND p.temp = false AND (:category IS NULL OR p.category = :category) ORDER BY p.id DESC")
    List<Post> findPosts(@Param("category") PostCategory category, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id < :lastPostId AND p.deleted = false AND p.temp = false AND (:category IS NULL OR p.category = :category) ORDER BY p.id DESC")
    List<Post> findPosts(@Param("lastPostId") Long lastPostId, @Param("category") PostCategory category, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId AND p.temp = false AND p.postTime > :since")
    long countRecentPostsByUser(@Param("userId") Long userId, @Param("since") Date since);

    @Query("SELECT p FROM Post p JOIN FETCH p.user u LEFT JOIN FETCH u.profileFile WHERE p.id = :postId")
    Optional<Post> findPost(@Param("postId") Long postId);

}
