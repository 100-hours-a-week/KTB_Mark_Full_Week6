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
public class PostReportId implements Serializable {
    private Long userId;
    private Long postId;

    public PostReportId(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }
}
