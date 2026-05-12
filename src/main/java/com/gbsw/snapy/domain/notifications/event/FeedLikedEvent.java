package com.gbsw.snapy.domain.notifications.event;

public record FeedLikedEvent(Long albumId, Long likeId, Long senderId, Long ownerId) {
}
