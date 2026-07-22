package com.mark.community.dto;

import lombok.Getter;

@Getter
public class LikeResponse {
    private long likeCount;

    public LikeResponse(long likeCount) {
        this.likeCount = likeCount;
    }
}
