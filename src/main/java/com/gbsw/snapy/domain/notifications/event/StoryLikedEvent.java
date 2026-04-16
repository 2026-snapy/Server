package com.gbsw.snapy.domain.notifications.event;

public record StoryLikedEvent(Long storyId, Long senderId, Long ownerId) {
}
