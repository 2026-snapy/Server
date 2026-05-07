package com.gbsw.snapy.domain.albums.dto.response;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AlbumDetailResponse(
        Long albumId,
        LocalDate albumDate,
        int photoCount,
        long likeCount,
        boolean liked,
        List<AlbumPhotoSet> photos
) {
    public static AlbumDetailResponse of(DailyAlbum album, long likeCount, boolean liked, List<AlbumPhotoSet> photos) {
        return new AlbumDetailResponse(
                album.getId(),
                album.getAlbumDate(),
                album.getPhotoCount(),
                likeCount,
                liked,
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
