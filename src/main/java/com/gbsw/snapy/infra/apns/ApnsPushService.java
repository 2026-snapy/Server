package com.gbsw.snapy.infra.apns;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.gbsw.snapy.domain.device.entity.DevicePlatform;
import com.gbsw.snapy.domain.device.entity.DeviceToken;
import com.gbsw.snapy.domain.device.entity.DeviceTokenEnvironment;
import com.gbsw.snapy.domain.device.repository.DeviceTokenRepository;
import com.gbsw.snapy.domain.notifications.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApnsPushService {

    private final ObjectProvider<ApnsClient> apnsClientProvider;
    private final ApnsProperties apnsProperties;
    private final DeviceTokenRepository deviceTokenRepository;
    private final Executor apnsResponseExecutor;

    public void sendToUser(Long userId, NotificationType type) {
        if (!apnsProperties.isEnabled()) {
            return;
        }

        ApnsClient apnsClient = apnsClientProvider.getIfAvailable();
        if (apnsClient == null) {
            log.warn("APNs client is not configured. userId: {}, type: {}", userId, type);
            return;
        }

        DeviceTokenEnvironment environment = apnsProperties.isProduction()
                ? DeviceTokenEnvironment.PRODUCTION
                : DeviceTokenEnvironment.SANDBOX;

        List<DeviceToken> deviceTokens = deviceTokenRepository.findByUserIdAndPlatformAndEnvironment(
                userId, DevicePlatform.IOS, environment);

        String payload = buildPayload(type);
        for (DeviceToken deviceToken : deviceTokens) {
            SimpleApnsPushNotification notification = new SimpleApnsPushNotification(
                    deviceToken.getToken(),
                    apnsProperties.getBundleId(),
                    payload
            );

            CompletableFuture<PushNotificationResponse<SimpleApnsPushNotification>> future;
            try {
                future = apnsClient.sendNotification(notification);
            } catch (Exception e) {
                log.warn("APNs push submission failed - userId: {}, tokenId: {}", userId, deviceToken.getId(), e);
                continue;
            }

            future.whenCompleteAsync((response, throwable) -> {
                if (throwable != null) {
                    log.warn("APNs push failed - userId: {}, tokenId: {}", userId, deviceToken.getId(), throwable);
                    return;
                }
                handleResponse(deviceToken, response);
            }, apnsResponseExecutor);
        }
    }

    private String buildPayload(NotificationType type) {
        ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
        payloadBuilder.setAlertTitle("Snapy");
        payloadBuilder.setAlertBody(messageOf(type));
        payloadBuilder.setSoundFileName("default");
        return payloadBuilder.build();
    }

    private String messageOf(NotificationType type) {
        return switch (type) {
            case ALBUM_PHOTO_UPLOAD_REMINDER -> "새 사진을 기록할 시간이에요.";
            case STORY_LIKE -> "회원님의 스토리에 하트가 도착했어요.";
            case FEED_LIKE -> "회원님의 피드에 좋아요가 도착했어요.";
            case FRIEND_REQUEST -> "새 친구 요청이 도착했어요.";
            case FRIEND_ACCEPTED -> "친구 요청이 수락됐어요.";
            case ALBUM_PUBLISHED -> "친구가 새 게시물을 업로드했어요.";
            case NEW_STORY -> "친구가 새 스토리를 업로드했어요.";
            case FEED_COMMENT -> "회원님의 피드에 새 댓글이 달렸어요.";
            case GUESTBOOK_CREATED -> "회원님의 프로필에 방명록이 남겨졌어요.";
        };
    }

    private void handleResponse(
            DeviceToken deviceToken,
            PushNotificationResponse<SimpleApnsPushNotification> response
    ) {
        if (response.isAccepted()) {
            return;
        }

        log.warn("APNs rejected push - tokenId: {}, reason: {}",
                deviceToken.getId(), response.getRejectionReason());

        response.getTokenInvalidationTimestamp().ifPresent(timestamp ->
                deviceTokenRepository.deleteById(deviceToken.getId()));
    }
}
