package com.mark.community.dto;

import java.util.List;

public class PostRequest {
    private String title;
    private String body;
    private List<String> images;
    private String category;

    public PostRequest(String title, String body, List<String> images, String category) {
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

    public List<String> getImages() {
        return images;
    }

    public String getCategory() {
        return category;
    }
}
