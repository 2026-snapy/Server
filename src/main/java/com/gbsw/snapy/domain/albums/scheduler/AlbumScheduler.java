package com.gbsw.snapy.domain.albums.scheduler;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.albums.service.AlbumCommandService;
import com.gbsw.snapy.domain.notifications.entity.NotificationType;
import com.gbsw.snapy.domain.notifications.service.NotificationService;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlbumScheduler {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private final AlbumCommandService albumCommandService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void autoPublishDraftAlbums() {
        LocalDate today = LocalDate.now(KST_ZONE);
        List<Long> draftAlbumIds = albumCommandService.findDraftAlbumIdsBefore(today);
        log.info("[AlbumScheduler] 자동 publish 대상 앨범 수: {}", draftAlbumIds.size());
        for (Long albumId : draftAlbumIds) {
            try {
                albumCommandService.autoPublishOne(albumId);
            } catch (Exception e) {
                log.error("[AlbumScheduler] 앨범 자동 publish 실패 - albumId: {}", albumId, e);
            }
        }
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
    public void remindMorningPhotoUpload() {
        createPhotoUploadReminders(AlbumPhotoType.MORNING);
    }

    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Seoul")
    public void remindLunchPhotoUpload() {
        createPhotoUploadReminders(AlbumPhotoType.LUNCH);
    }

    @Scheduled(cron = "0 0 17 * * *", zone = "Asia/Seoul")
    public void remindDinnerPhotoUpload() {
        createPhotoUploadReminders(AlbumPhotoType.DINNER);
    }

    private void createPhotoUploadReminders(AlbumPhotoType type) {
        List<Long> userIds = userRepository.findAllIds();
        log.info("[AlbumScheduler] 사진 업로드 리마인더 생성 - type: {}, userCount: {}", type, userIds.size());
        for (Long userId : userIds) {
            try {
                notificationService.create(
                        userId, userId,
                        NotificationType.ALBUM_PHOTO_UPLOAD_REMINDER, null, type.name()
                );
            } catch (Exception e) {
                log.warn("[AlbumScheduler] 사진 업로드 리마인더 생성 실패 - userId: {}, type: {}", userId, type, e);
            }
        }
    }
}
