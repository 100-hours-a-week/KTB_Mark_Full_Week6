package com.mark.community.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mark.community.enums.NotificationType;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class NotificationResponse {
    private Long notificationId;
    private NotificationType type;
    private String message;
    private Long postId;
    private Long commentId;
    private boolean read;
    private String createdAt;

    public NotificationResponse(Long notificationId, NotificationType type, String message, Long postId, Long commentId, boolean read, String createdAt) {
        this.notificationId = notificationId;
        this.type = type;
        this.message = message;
        this.postId = postId;
        this.commentId = commentId;
        this.read = read;
        this.createdAt = createdAt;
    }
}
