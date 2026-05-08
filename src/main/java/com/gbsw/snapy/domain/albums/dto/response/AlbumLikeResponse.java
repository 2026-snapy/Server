package com.gbsw.snapy.domain.albums.dto.response;

public record AlbumLikeResponse(
        Long albumId,
        boolean liked,
        long likeCount
) {
}
