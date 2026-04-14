package com.gbsw.snapy.domain.stories.dto.response;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;

import java.time.LocalDateTime;
import java.util.List;

public record StoryDetailResponse(
        Long storyId,
        String handle,
        String username,
        String profileImageUrl,
        List<StoryPhotoSet> photos,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
    public record StoryPhotoSet(
            AlbumPhotoType type,
            String frontImageUrl,
            String backImageUrl,
            LocalDateTime createdAt
    ) {
    }
}
