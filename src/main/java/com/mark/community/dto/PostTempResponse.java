package com.mark.community.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostTempResponse {
    private Long postId;
    private List<Long> images;
    private String title;
    private String body;
    private String category;

    public PostTempResponse(Long postId){
        this.postId = postId;
    }

    public PostTempResponse(Long postId, List<Long> images){
        this.postId = postId;
        this.images = images;
    }

    public PostTempResponse(Long postId, String title, String body, List<Long> images, String category){
        this.postId = postId;
        this.title = title;
        this.body = body;
        this.images = images;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public List<Long> getImages() {
        return images;
    }

    public String getCategory() {
        return category;
    }
}
