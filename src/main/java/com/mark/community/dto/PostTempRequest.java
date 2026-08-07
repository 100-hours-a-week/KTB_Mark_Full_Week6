package com.mark.community.dto;


public class PostTempRequest {
    private String title;
    private String body;
    private String category;

    public PostTempRequest(String title, String body, String category) {
        this.title = title;
        this.body = body;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }
    public String getBody() {
        return body;
    }
    public String getCategory() {
        return category;
    }
}
