package com.gbsw.snapy.infra.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.gbsw.snapy.domain.device.entity.DevicePlatform;
import com.gbsw.snapy.domain.device.entity.DeviceToken;
import com.gbsw.snapy.domain.device.repository.DeviceTokenRepository;
import com.gbsw.snapy.domain.notifications.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    private final FcmProperties fcmProperties;
    private final DeviceTokenRepository deviceTokenRepository;

    public void sendToUser(Long userId, NotificationType type) {
        if (!fcmProperties.isEnabled()) {
            return;
        }

        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            log.warn("FCM client is not configured. userId: {}, type: {}", userId, type);
            return;
        }

        List<DeviceToken> deviceTokens = deviceTokenRepository.findByUserIdAndPlatform(userId, DevicePlatform.ANDROID);
        for (DeviceToken deviceToken : deviceTokens) {
            try {
                firebaseMessaging.send(Message.builder()
                        .setToken(deviceToken.getToken())
                        .setNotification(Notification.builder()
                                .setTitle("Snapy")
                                .setBody(messageOf(type))
                                .build())
                        .putData("type", type.name())
                        .build());
            } catch (FirebaseMessagingException e) {
                handleFailure(deviceToken, e);
            } catch (Exception e) {
                log.warn("FCM push failed - userId: {}, tokenId: {}", userId, deviceToken.getId(), e);
            }
        }
    }

    private String messageOf(NotificationType type) {
        return switch (type) {
            case ALBUM_PHOTO_UPLOAD_REMINDER -> "사진을 기록할 시간이에요.";
            case STORY_LIKE -> "회원님의 스토리에 하트가 도착했어요.";
            case FEED_LIKE -> "회원님의 피드에 좋아요가 도착했어요.";
            case FRIEND_REQUEST -> "새 친구 요청이 도착했어요.";
            case FRIEND_ACCEPTED -> "친구 요청이 수락됐어요.";
            case ALBUM_PUBLISHED -> "친구가 새 게시물을 업로드했어요.";
            case NEW_STORY -> "친구가 새 스토리를 업로드했어요.";
            case FEED_COMMENT -> "회원님의 피드에 댓글이 달렸어요.";
            case GUESTBOOK_CREATED -> "회원님의 프로필에 방명록이 남겨졌어요.";
        };
    }

    private void handleFailure(DeviceToken deviceToken, FirebaseMessagingException e) {
        log.warn("FCM rejected push - tokenId: {}, reason: {}", deviceToken.getId(), e.getMessagingErrorCode(), e);

        if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
            deviceTokenRepository.deleteById(deviceToken.getId());
        }
    }
}
