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
        try {
            notificationService.create(
                    event.ownerId(), event.senderId(),
                    NotificationType.STORY_LIKE, event.storyId(), event.type().name()
            );
        } catch (Exception e) {
            log.warn("스토리 좋아요 알림 생성 실패 - storyId: {}, type: {}",
                    event.storyId(), event.type(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedLiked(FeedLikedEvent event) {
        try {
            notificationService.createFeedLikeIfAbsent(
                    event.ownerId(), event.senderId(), event.likeId(), event.albumId());
        } catch (Exception e) {
            log.warn("피드 좋아요 알림 생성 실패 - albumId: {}, likeId: {}",
                    event.albumId(), event.likeId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedComment(FeedCommentEvent event) {
        try {
            notificationService.create(
                    event.ownerId(), event.senderId(),
                    NotificationType.FEED_COMMENT, event.commentId(), String.valueOf(event.albumId())
            );
        } catch (Exception e) {
            log.warn("피드 댓글 알림 생성 실패 - commentId: {}, albumId: {}",
                    event.commentId(), event.albumId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGuestbookCreated(GuestbookCreatedEvent event) {
        try {
            notificationService.create(
                    event.ownerId(), event.authorId(),
                    NotificationType.GUESTBOOK_CREATED, event.authorId(), String.valueOf(event.ownerId())
            );
        } catch (Exception e) {
            log.warn("방명록 알림 생성 실패 - ownerId: {}, authorId: {}",
                    event.ownerId(), event.authorId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendRequest(FriendRequestEvent event) {
        try {
            notificationService.create(
                    event.receiverId(), event.senderId(),
                    NotificationType.FRIEND_REQUEST, event.requestId(), null
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
                    NotificationType.FRIEND_ACCEPTED, null, null
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
                if (event.pushEnabled()) {
                    notificationService.create(
                            friendId, event.userId(),
                            NotificationType.ALBUM_PUBLISHED, event.albumId(), null
                    );
                } else {
                    notificationService.createWithoutPush(
                            friendId, event.userId(),
                            NotificationType.ALBUM_PUBLISHED, event.albumId(), null
                    );
                }
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
                        NotificationType.NEW_STORY, event.storyId(), null
                );
            } catch (Exception e) {
                log.warn("새 스토리 알림 생성 실패 - storyId: {}, friendId: {}",
                        event.storyId(), friendId, e);
            }
        }
    }
}
