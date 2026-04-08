package com.gbsw.snapy.domain.albums.dto.response;

import com.gbsw.snapy.domain.albums.entity.DailyAlbum;

import java.time.LocalDate;

public record AlbumListResponse(
        Long albumId,
        LocalDate albumDate,
        String thumbnailUrl
) {
    public static AlbumListResponse of(DailyAlbum album, String thumbnailUrl) {
        return new AlbumListResponse(
                album.getId(),
                album.getAlbumDate(),
                thumbnailUrl
        );
    }
}
