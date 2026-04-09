package com.gbsw.snapy.domain.albums.dto.response;

import com.gbsw.snapy.domain.albums.entity.AlbumStatus;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;

import java.time.LocalDateTime;

public record AlbumPublishResponse(
        Long albumId,
        AlbumStatus status,
        LocalDateTime publishedAt
) {
    public static AlbumPublishResponse from(DailyAlbum album) {
        return new AlbumPublishResponse(
                album.getId(),
                album.getStatus(),
                album.getPublishedAt()
        );
    }
}
