package com.mark.community.controller;

import com.mark.community.dto.CustomUserDetails;
import com.mark.community.dto.NotificationSummaryResponse;
import com.mark.community.messages.ApiResponseMessage;
import com.mark.community.response.ApiResponse;
import com.mark.community.service.NotificationService;
import com.mark.community.service.SseEmitterService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    public NotificationController(NotificationService notificationService, SseEmitterService sseEmitterService) {
        this.notificationService = notificationService;
        this.sseEmitterService = sseEmitterService;
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        SseEmitter emitter = sseEmitterService.subscribe(userDetails.getId());

        if (lastEventId != null) {
            try {
                notificationService.replayMissedNotifications(userDetails.getId(), Long.parseLong(lastEventId), emitter);
            } catch (NumberFormatException e) {

            }
        }

        return emitter;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestParam(defaultValue = "20") int size,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        NotificationSummaryResponse response = notificationService.getNotifications(userDetails.getId(), size);

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_GET_NOTIFICATIONS.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_GET_NOTIFICATIONS, response));
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAsRead(notificationId, userDetails.getId());

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_UPDATE_NOTIFICATION.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_UPDATE_NOTIFICATION));
    }

    @PatchMapping
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getId());

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_UPDATE_ALL_NOTIFICATIONS.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_UPDATE_ALL_NOTIFICATIONS));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.deleteNotification(notificationId, userDetails.getId());

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_DELETE_NOTIFICATION.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_DELETE_NOTIFICATION));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.deleteAllNotifications(userDetails.getId());

        return ResponseEntity
                .status(ApiResponseMessage.SUCCESS_DELETE_ALL_NOTIFICATIONS.getStatusCode())
                .body(new ApiResponse<>(ApiResponseMessage.SUCCESS_DELETE_ALL_NOTIFICATIONS));
    }
}
