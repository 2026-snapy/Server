package com.gbsw.snapy.domain.albums.dto.response;

public record ProfilePastAlbumResponse(
        int year,
        int month,
        String thumbnailUrl
) {
}
