package com.mark.community.service;

import com.mark.community.dto.NotificationResponse;
import com.mark.community.dto.NotificationSummaryResponse;
import com.mark.community.entity.Notification;
import com.mark.community.entity.User;
import com.mark.community.enums.NotificationType;
import com.mark.community.exception.CustomException;
import com.mark.community.messages.ApiResponseErrorMessage;
import com.mark.community.repository.NotificationRepository;
import com.mark.community.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseEmitterService sseEmitterService;

    public NotificationService(NotificationRepository notificationRepository,
                                UserRepository userRepository,
                                SseEmitterService sseEmitterService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.sseEmitterService = sseEmitterService;
    }

    public void notifyPostLiked(Long postAuthorId, Long actorId, Long postId) {
        if (actorId.equals(postAuthorId)) return;
        persistAndPush(postAuthorId, actorId, NotificationType.LIKE, postId, null);
    }

    public void notifyNewComment(Long postAuthorId, Long actorId, Long postId, Long commentId) {
        if (actorId.equals(postAuthorId)) return;
        persistAndPush(postAuthorId, actorId, NotificationType.COMMENT, postId, commentId);
    }

    public void notifyNewReply(Long postAuthorId, Long parentCommentAuthorId, Long actorId, Long postId, Long commentId) {
        Set<Long> recipients = new HashSet<>();
        if (!postAuthorId.equals(actorId)) recipients.add(postAuthorId);
        if (!parentCommentAuthorId.equals(actorId)) recipients.add(parentCommentAuthorId);

        for (Long recipientId : recipients) {
            persistAndPush(recipientId, actorId, NotificationType.REPLY, postId, commentId);
        }
    }

    private void persistAndPush(Long recipientId, Long actorId, NotificationType type, Long postId, Long commentId) {
        Notification notification = notificationRepository.save(
                new Notification(recipientId, actorId, type, postId, commentId, new Date())
        );
        NotificationResponse response = toResponse(notification, resolveActorNickname(actorId));
        sseEmitterService.sendToUser(recipientId, "notification", String.valueOf(notification.getId()), response);
    }

    public void replayMissedNotifications(Long userId, Long lastEventId, SseEmitter emitter) {
        List<Notification> missed = notificationRepository.findReplayMissedNotifications(userId, lastEventId);
        for (Notification notification : missed) {
            NotificationResponse response = toResponse(notification, resolveActorNickname(notification.getActorId()));
            sseEmitterService.sendToEmitter(userId, emitter, "notification", String.valueOf(notification.getId()), response);
        }
    }

    @Transactional(readOnly = true)
    public NotificationSummaryResponse getNotifications(Long userId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<Notification> notifications = notificationRepository.findNotificationList(userId, pageable);

        Set<Long> actorIds = notifications.stream().map(Notification::getActorId).collect(Collectors.toSet());

        Map<Long, String> nicknames = userRepository.findAllById(actorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        List<NotificationResponse> responses = notifications.stream()
                .map(n -> toResponse(n, nicknames.getOrDefault(n.getActorId(), "알 수 없음")))
                .toList();

        long unreadCount = notificationRepository.countNotifications(userId);
        return new NotificationSummaryResponse(responses, unreadCount);
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findNotification(notificationId, userId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.NOTIFICATION_NOT_FOUND));
        notification.setRead(true);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findUnreadNotifications(userId);
        unread.forEach(notification -> notification.setRead(true));
    }

    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findNotification(notificationId, userId)
                .orElseThrow(() -> new CustomException(ApiResponseErrorMessage.NOTIFICATION_NOT_FOUND));
        notification.setDeleted(true);
    }

    public void deleteAllNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findAllNotifications(userId);
        notifications.forEach(notification -> notification.setDeleted(true));
    }

    private String resolveActorNickname(Long actorId) {
        return userRepository.findById(actorId).map(User::getNickname).orElse("알 수 없음");
    }

    private NotificationResponse toResponse(Notification notification, String actorNickname) {
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                buildMessage(notification.getType(), actorNickname),
                notification.getPostId(),
                notification.getCommentId(),
                notification.isRead(),
                sd.format(notification.getCreatedAt())
        );
    }

    private String buildMessage(NotificationType type, String actorNickname) {
        return switch (type) {
            case LIKE -> actorNickname + "님이 회원님의 게시글을 좋아합니다.";
            case COMMENT -> actorNickname + "님이 댓글을 남겼습니다.";
            case REPLY -> actorNickname + "님이 답글을 남겼습니다.";
        };
    }
}
