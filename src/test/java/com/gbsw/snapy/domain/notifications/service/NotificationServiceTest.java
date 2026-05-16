package com.gbsw.snapy.domain.notifications.service;

import com.gbsw.snapy.domain.notifications.entity.Notification;
import com.gbsw.snapy.domain.notifications.entity.NotificationType;
import com.gbsw.snapy.domain.notifications.repository.NotificationRepository;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.infra.apns.ApnsPushService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApnsPushService apnsPushService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createFeedLikeIfAbsentSkipsDuplicateNotificationAndPush() {
        when(notificationRepository.existsByDeduplicationKey("FEED_LIKE:1:2:10")).thenReturn(true);

        notificationService.createFeedLikeIfAbsent(1L, 2L, 100L, 10L);

        verify(notificationRepository, never()).saveAndFlush(any(Notification.class));
        verify(apnsPushService, never()).sendToUser(any(), any());
    }

    @Test
    void createFeedLikeIfAbsentCreatesNotificationAndPushWhenNotExists() {
        when(notificationRepository.existsByDeduplicationKey("FEED_LIKE:1:2:10")).thenReturn(false);

        notificationService.createFeedLikeIfAbsent(1L, 2L, 100L, 10L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).saveAndFlush(captor.capture());
        Notification notification = captor.getValue();
        assertThat(notification.getReceiverId()).isEqualTo(1L);
        assertThat(notification.getSenderId()).isEqualTo(2L);
        assertThat(notification.getType()).isEqualTo(NotificationType.FEED_LIKE);
        assertThat(notification.getReferenceId()).isEqualTo(100L);
        assertThat(notification.getReferenceType()).isEqualTo("10");
        assertThat(notification.getDeduplicationKey()).isEqualTo("FEED_LIKE:1:2:10");
        assertThat(notification.isRead()).isFalse();
        verify(apnsPushService).sendToUser(1L, NotificationType.FEED_LIKE);
    }

    @Test
    void createFeedLikeIfAbsentSkipsPushWhenConcurrentInsertWins() {
        when(notificationRepository.existsByDeduplicationKey("FEED_LIKE:1:2:10")).thenReturn(false);
        when(notificationRepository.saveAndFlush(any(Notification.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        notificationService.createFeedLikeIfAbsent(1L, 2L, 100L, 10L);

        verify(apnsPushService, never()).sendToUser(any(), any());
    }
}
