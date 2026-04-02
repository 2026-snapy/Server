package com.gbsw.snapy.domain.albums.dto.response;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;

import java.time.LocalDate;

public record AlbumUploadResponse(
        Long albumId,
        LocalDate albumDate,
        AlbumPhotoType type,
        int photoCount
) {
    public static AlbumUploadResponse from(DailyAlbum album, AlbumPhotoType type) {
        return new AlbumUploadResponse(
                album.getId(),
                album.getAlbumDate(),
                type,
                album.getPhotoCount()
        );
    }
}
