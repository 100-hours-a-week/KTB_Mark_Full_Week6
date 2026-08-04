package com.mark.community.entity;

import com.mark.community.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "notification")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long recipientId;
    private Long actorId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private Long postId;
    private Long commentId;

    @Column(name = "is_read")
    private boolean read = false;

    private Date createdAt;

    public Notification(Long recipientId, Long actorId, NotificationType type, Long postId, Long commentId, Date createdAt) {
        this.recipientId = recipientId;
        this.actorId = actorId;
        this.type = type;
        this.postId = postId;
        this.commentId = commentId;
        this.createdAt = createdAt;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
