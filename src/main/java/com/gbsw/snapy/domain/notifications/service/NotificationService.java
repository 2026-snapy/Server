package com.gbsw.snapy.domain.notifications.service;

import com.gbsw.snapy.domain.notifications.dto.response.NotificationPageResponse;
import com.gbsw.snapy.domain.notifications.dto.response.NotificationResponse;
import com.gbsw.snapy.domain.notifications.dto.response.UnreadCountResponse;
import com.gbsw.snapy.domain.notifications.entity.Notification;
import com.gbsw.snapy.domain.notifications.entity.NotificationType;
import com.gbsw.snapy.domain.notifications.repository.NotificationRepository;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void create(Long receiverId, Long senderId, NotificationType type,
                       Long referenceId, String referenceType) {
        notificationRepository.save(
                Notification.builder()
                        .receiverId(receiverId)
                        .senderId(senderId)
                        .type(type)
                        .referenceId(referenceId)
                        .referenceType(referenceType)
                        .read(false)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(Long userId, Pageable pageable) {
        Slice<Notification> slice = notificationRepository
                .findByReceiverIdOrderByCreatedAtDesc(userId, pageable);

        if (!slice.hasContent()) {
            return NotificationPageResponse.of(new SliceImpl<>(List.of(), pageable, false));
        }

        List<Long> senderIds = slice.getContent().stream()
                .map(Notification::getSenderId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<NotificationResponse> items = new ArrayList<>();
        for (Notification notification : slice.getContent()) {
            User sender = userMap.get(notification.getSenderId());
            items.add(NotificationResponse.of(
                    notification,
                    sender != null ? sender.getHandle() : null,
                    sender != null ? sender.getUsername() : null,
                    sender != null ? sender.getProfileImageUrl() : null
            ));
        }

        Slice<NotificationResponse> mapped = new SliceImpl<>(items, pageable, slice.hasNext());
        return NotificationPageResponse.of(mapped);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countByReceiverIdAndReadFalse(userId);
        return new UnreadCountResponse(count);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiverId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
