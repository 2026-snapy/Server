package com.gbsw.snapy.infra.push;

import com.gbsw.snapy.domain.notifications.entity.NotificationType;
import com.gbsw.snapy.infra.apns.ApnsPushService;
import com.gbsw.snapy.infra.fcm.FcmPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final ApnsPushService apnsPushService;
    private final FcmPushService fcmPushService;

    public void sendToUser(Long userId, NotificationType type) {
        try {
            apnsPushService.sendToUser(userId, type);
        } catch (Exception e) {
            log.warn("APNs push failed - userId: {}, type: {}", userId, type, e);
        }

        try {
            fcmPushService.sendToUser(userId, type);
        } catch (Exception e) {
            log.warn("FCM push failed - userId: {}, type: {}", userId, type, e);
        }
    }
}
