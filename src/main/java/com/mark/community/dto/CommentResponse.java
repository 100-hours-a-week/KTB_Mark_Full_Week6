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
    private String userRole;
    private boolean permission;


    public CommentResponse(Long commentId, String comment ,String userRole){
        this.commentId = commentId;
        this.comment = comment;
        this.userRole = userRole;
    }

    public CommentResponse(Long commentId, String nickname, String comment, Long userId, Long parentCommentId, boolean deleted, boolean permission){
        this.commentId = commentId;
        this.nickname = nickname;
        this.comment = comment;
        this.userId = userId;
        this.parentCommentId = parentCommentId;
        this.deleted = deleted;
        this.permission = permission;
    }
}
