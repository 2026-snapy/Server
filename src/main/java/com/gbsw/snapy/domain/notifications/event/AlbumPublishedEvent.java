package com.gbsw.snapy.domain.notifications.event;

public record AlbumPublishedEvent(Long albumId, Long userId, boolean pushEnabled) {

    public AlbumPublishedEvent(Long albumId, Long userId) {
        this(albumId, userId, true);
    }
}
