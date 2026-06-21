package com.mark.community.entity;


import com.mark.community.entity.key.PostImageId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "post_image")
public class PostImage {
    @EmbeddedId
    private PostImageId id;

    public PostImage(Long fileId, Long postId){
        this.id = new PostImageId(fileId, postId);
    }


}
