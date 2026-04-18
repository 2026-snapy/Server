package com.gbsw.snapy.domain.notifications.event;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;

public record StoryLikedEvent(Long storyId, Long senderId, Long ownerId, AlbumPhotoType type) {
}
