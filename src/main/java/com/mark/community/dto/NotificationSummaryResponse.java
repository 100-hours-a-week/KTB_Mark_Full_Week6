package com.mark.community.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class NotificationSummaryResponse {
    private List<NotificationResponse> notifications;
    private long unreadCount;

    public NotificationSummaryResponse(List<NotificationResponse> notifications, long unreadCount) {
        this.notifications = notifications;
        this.unreadCount = unreadCount;
    }
}
