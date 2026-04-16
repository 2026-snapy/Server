package com.gbsw.snapy.domain.stories.dto.response;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;

public record StoryLikeResponse(
        Long storyId,
        AlbumPhotoType type,
        boolean liked
) {
}
