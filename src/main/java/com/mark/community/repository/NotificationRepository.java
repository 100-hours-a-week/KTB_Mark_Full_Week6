package com.mark.community.repository;

import com.mark.community.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.deleted = false ORDER BY n.createdAt DESC")
    List<Notification> findNotificationList(@Param("recipientId") Long recipientId, Pageable pageable);

    @Query("SELECT Count(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.read = false AND n.deleted = false")
    long countNotifications(@Param("recipientId") Long recipientId);

    @Query("SELECT n FROM Notification n WHERE n.id = :id AND n.recipientId = :recipientId AND n.deleted = false")
    Optional<Notification> findNotification (@Param("id") Long id, @Param("recipientId") Long recipientId);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.id > :id AND n.deleted = false ORDER BY n.id ASC")
    List<Notification> findReplayMissedNotifications(@Param("recipientId") Long recipientId, @Param("id") Long id);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.deleted = false AND n.read = false")
    List<Notification> findUnreadNotifications(@Param("recipientId") Long recipientId);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.deleted = false")
    List<Notification> findAllNotifications(@Param("recipientId") Long recipientId);
}
