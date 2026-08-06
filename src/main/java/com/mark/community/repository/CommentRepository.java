package com.mark.community.repository;

import com.mark.community.dto.PostCount;
import com.mark.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByPostId(Long postId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId")
    List<Comment> findByPostId(@Param("postId") Long postId);
    long countByUserId(Long userId);

    @Query("SELECT c.post.id AS postId, COUNT(c) AS count FROM Comment c WHERE c.post.id IN :postIds GROUP BY c.post.id")
    List<PostCount> countGroupedByPostIds(@Param("postIds") List<Long> postIds);

}
