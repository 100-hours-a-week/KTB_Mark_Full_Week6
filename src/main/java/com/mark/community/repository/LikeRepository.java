package com.mark.community.repository;

import com.mark.community.dto.PostCount;
import com.mark.community.entity.Like;
import com.mark.community.entity.key.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, PostLikeId> {
    long countByIdPostId(Long postId);

    @Query("SELECT l.id.postId AS postId, COUNT(l) AS count FROM Like AS l WHERE l.id.postId IN :postIds GROUP BY l.id.postId")
    List<PostCount> countGroupByPostIds(@Param("postIds") List<Long> postIds);

}
