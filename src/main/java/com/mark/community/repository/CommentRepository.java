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
    List<Comment> findByPostId(Long postId);
    long countByUserId(Long userId);

    @Query("SELECT comments.post.id AS postId, COUNT(comments) AS count FROM Comment comments WHERE comments.post.id IN :postIds GROUP BY comments.post.id")
    List<PostCount> countGroupedByPostIds(@Param("postIds") List<Long> postIds);

}
