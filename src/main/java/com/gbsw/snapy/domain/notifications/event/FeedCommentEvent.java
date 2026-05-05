package com.gbsw.snapy.domain.notifications.event;

public record FeedCommentEvent(Long commentId, Long albumId, Long senderId, Long ownerId) {
}
