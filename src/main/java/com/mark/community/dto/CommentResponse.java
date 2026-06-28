package com.mark.community.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class CommentResponse {
    private Long commentId;
    private String nickname;
    private String comment;
    private Long parentCommentId;
    private Long userId;
    private boolean deleted;

    public CommentResponse(Long commentId){
        this.commentId = commentId;
    }

    public CommentResponse(Long commentId, String nickname, String comment, Long userId, Long parentCommentId , boolean deleted){
        this.commentId = commentId;
        this.nickname = nickname;
        this.comment = comment;
        this.userId = userId;
        this.parentCommentId = parentCommentId;
        this.deleted = deleted;
    }
}
