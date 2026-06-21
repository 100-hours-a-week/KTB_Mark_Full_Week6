package com.mark.community.entity.key;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class PostImageId implements Serializable {
    private Long fileId;
    private Long postId;

    public PostImageId(Long fileId, Long postId){
        this.fileId = fileId;
        this.postId = postId;
    }

}
