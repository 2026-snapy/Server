package com.gbsw.snapy.domain.albums.dto.response;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AlbumTodayResponse(
        Long albumId,
        LocalDate albumDate,
        int photoCount,
        List<AlbumPhotoSet> photos
) {
    public static AlbumTodayResponse of(DailyAlbum album, List<AlbumPhotoSet> photos) {
        return new AlbumTodayResponse(
                album.getId(),
                album.getAlbumDate(),
                album.getPhotoCount(),
                photos
        );
    }

    public record AlbumPhotoSet(
            AlbumPhotoType type,
            String frontImageUrl,
            String backImageUrl,
            LocalDateTime createdAt
    ) {
    }
}
