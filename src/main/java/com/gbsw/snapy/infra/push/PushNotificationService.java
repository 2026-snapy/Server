package com.gbsw.snapy.infra.push;

import com.gbsw.snapy.domain.notifications.entity.NotificationType;
import com.gbsw.snapy.infra.apns.ApnsPushService;
import com.gbsw.snapy.infra.fcm.FcmPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final ApnsPushService apnsPushService;
    private final FcmPushService fcmPushService;

    public void sendToUser(Long userId, NotificationType type) {
        apnsPushService.sendToUser(userId, type);
        fcmPushService.sendToUser(userId, type);
    }
}
