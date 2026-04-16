package com.gbsw.snapy.domain.notifications.dto.response;

import com.gbsw.snapy.domain.notifications.entity.Notification;
import com.gbsw.snapy.domain.notifications.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long senderId,
        String senderHandle,
        String senderUsername,
        String senderProfileImageUrl,
        NotificationType type,
        Long referenceId,
        String referenceType,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse of(Notification notification,
                                           String senderHandle,
                                           String senderUsername,
                                           String senderProfileImageUrl) {
        return new NotificationResponse(
                notification.getId(),
                notification.getSenderId(),
                senderHandle,
                senderUsername,
                senderProfileImageUrl,
                notification.getType(),
                notification.getReferenceId(),
                notification.getReferenceType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
