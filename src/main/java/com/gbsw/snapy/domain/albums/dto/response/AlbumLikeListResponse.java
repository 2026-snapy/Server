package com.gbsw.snapy.domain.albums.dto.response;

import java.time.LocalDateTime;

public record AlbumLikeListResponse(
        Long userId,
        String handle,
        String username,
        String profileImageUrl,
        LocalDateTime likedAt
) {
}
