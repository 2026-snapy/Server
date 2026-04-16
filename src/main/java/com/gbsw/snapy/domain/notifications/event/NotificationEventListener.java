package com.gbsw.snapy.domain.notifications.event;

import com.gbsw.snapy.domain.friends.repository.FriendRepository;
import com.gbsw.snapy.domain.notifications.entity.NotificationType;
import com.gbsw.snapy.domain.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final FriendRepository friendRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStoryLiked(StoryLikedEvent event) {
        if (event.senderId().equals(event.ownerId())) {
            return;
        }

        try {
            notificationService.create(
                    event.ownerId(), event.senderId(),
                    NotificationType.STORY_LIKE, event.storyId()
            );
        } catch (Exception e) {
            log.warn("스토리 좋아요 알림 생성 실패 - storyId: {}", event.storyId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendRequest(FriendRequestEvent event) {
        try {
            notificationService.create(
                    event.receiverId(), event.senderId(),
                    NotificationType.FRIEND_REQUEST, event.requestId()
            );
        } catch (Exception e) {
            log.warn("친구 요청 알림 생성 실패 - requestId: {}", event.requestId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendAccepted(FriendAcceptedEvent event) {
        try {
            notificationService.create(
                    event.senderId(), event.receiverId(),
                    NotificationType.FRIEND_ACCEPTED, null
            );
        } catch (Exception e) {
            log.warn("친구 수락 알림 생성 실패", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlbumPublished(AlbumPublishedEvent event) {
        List<Long> friendIds = friendRepository.findFriendIdsByUserId(event.userId());
        for (Long friendId : friendIds) {
            try {
                notificationService.create(
                        friendId, event.userId(),
                        NotificationType.ALBUM_PUBLISHED, event.albumId()
                );
            } catch (Exception e) {
                log.warn("앨범 게시 알림 생성 실패 - albumId: {}, friendId: {}",
                        event.albumId(), friendId, e);
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewStory(NewStoryEvent event) {
        List<Long> friendIds = friendRepository.findFriendIdsByUserId(event.userId());
        for (Long friendId : friendIds) {
            try {
                notificationService.create(
                        friendId, event.userId(),
                        NotificationType.NEW_STORY, event.storyId()
                );
            } catch (Exception e) {
                log.warn("새 스토리 알림 생성 실패 - storyId: {}, friendId: {}",
                        event.storyId(), friendId, e);
            }
        }
    }
}
