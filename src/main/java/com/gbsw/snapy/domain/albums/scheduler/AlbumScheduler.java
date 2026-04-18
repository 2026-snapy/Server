package com.gbsw.snapy.domain.albums.scheduler;

import com.gbsw.snapy.domain.albums.service.AlbumService;
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

    private final AlbumService albumService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void autoPublishDraftAlbums() {
        LocalDate today = LocalDate.now(KST_ZONE);
        List<Long> draftAlbumIds = albumService.findDraftAlbumIdsBefore(today);
        log.info("[AlbumScheduler] 자동 publish 대상 앨범 수: {}", draftAlbumIds.size());
        for (Long albumId : draftAlbumIds) {
            try {
                albumService.autoPublishOne(albumId);
            } catch (Exception e) {
                log.error("[AlbumScheduler] 앨범 자동 publish 실패 - albumId: {}", albumId, e);
            }
        }
    }
}
