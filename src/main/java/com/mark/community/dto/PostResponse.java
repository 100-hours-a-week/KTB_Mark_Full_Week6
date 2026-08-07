package com.mark.community.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class PostResponse {
    private Long postId;
    private String title;
    private String body;
    private Long thumbnailId;
    private String nickname;
    private Long userId;
    private Counts counts;
    private String postTime;
    private List<Long> fileIds;

    private boolean deleted;
    private boolean blind;
    private boolean edited;
    private boolean permission;
    private String category;



    public PostResponse(Long postId){
        this.postId = postId;
    }

    public PostResponse(Long postId,
                        String title,
                        String body,
                        Long thumbnailId,
                        String nickname,
                        Long userId,
                        Counts counts,
                        List<Long> fileIds,
                        boolean edited,
                        boolean permission,
                        String postTime,
                        String category
    ){
        this.postId = postId;
        this.title = title;
        this.body = body;
        this.thumbnailId = thumbnailId;
        this.nickname = nickname;
        this.userId = userId;
        this.counts = counts;
        this.fileIds = fileIds;
        this.edited = edited;
        this.permission = permission;
        this.postTime = postTime;
        this.category = category;
    }

    public PostResponse(Long postId,
                        String title,
                        String body,
                        Long thumbnailId,
                        String nickname,
                        Long userId,
                        Counts counts,
                        String postTime,
                        boolean deleted,
                        boolean blind,
                        String category){
        this.postId = postId;
        this.title = title;
        this.body = body;
        this.thumbnailId = thumbnailId;
        this.nickname = nickname;
        this.userId = userId;
        this.counts = counts;
        this.postTime = postTime;
        this.deleted = deleted;
        this.blind = blind;
        this.category = category;
    }

    public PostResponse(){

    }





}
