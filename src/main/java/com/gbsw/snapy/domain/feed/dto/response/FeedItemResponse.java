package com.gbsw.snapy.domain.feed.dto.response;

import com.gbsw.snapy.domain.albums.dto.response.AlbumDetailResponse;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;
import com.gbsw.snapy.domain.users.entity.User;

import java.time.LocalDate;
import java.util.List;

public record FeedItemResponse(
        Long albumId,
        LocalDate albumDate,
        int photoCount,
        long likeCount,
        boolean liked,
        List<AlbumDetailResponse.AlbumPhotoSet> photos,
        String authorName,
        String authorHandle
) {
    public static FeedItemResponse of(DailyAlbum album, long likeCount, boolean liked,
                                      List<AlbumDetailResponse.AlbumPhotoSet> photos, User user) {
        return new FeedItemResponse(
                album.getId(),
                album.getAlbumDate(),
                album.getPhotoCount(),
                likeCount,
                liked,
                photos,
                user.getUsername(),
                user.getHandle()
        );
    }
}
